package com.telemetry.engine.consumer.service.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.telemetry.engine.common.constansts.H3Constants;
import com.telemetry.engine.common.geo.H3Service;
import com.telemetry.engine.common.mapper.JsonMapperUtil;
import com.telemetry.engine.common.redis.RedisService;
import com.telemetry.engine.consumer.dto.MessageEvent;
import com.telemetry.engine.consumer.entity.AssetCurrentLocation;
import com.telemetry.engine.consumer.entity.AssetLocationHistory;
import com.telemetry.engine.consumer.metrics.DailyCounter;
import com.telemetry.engine.consumer.persistence.AssetCurrentLocationPersistence;
import com.telemetry.engine.consumer.persistence.AssetLocationHistoryPersistence;
import com.telemetry.engine.consumer.service.Processor;
import io.r2dbc.postgresql.codec.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorImpl implements Processor {

  private final AssetLocationHistoryPersistence historyPersistence;
  private final AssetCurrentLocationPersistence currentPersistence;
  private final DailyCounter dailyCounter;
  private final H3Service h3Service;
  private final RedisService redisService;


  @Override
  public Mono<Void> process(String jsonArrayValue) {
    log.info("RECEIVED DATA={}", jsonArrayValue);
    if (jsonArrayValue == null || jsonArrayValue.isBlank()) {
      return Mono.empty();
    }

    return Mono
        .fromCallable(() -> JsonMapperUtil.deserializeJsonToList(jsonArrayValue, MessageEvent.class))
        .subscribeOn(Schedulers.boundedElastic()).flatMap(events -> {

          if (events.isEmpty()) {
            return Mono.empty();
          }


          // Take latest event
          MessageEvent latestEvent =
              events.stream().max(Comparator.comparing(MessageEvent::getDeviceTs)).orElseThrow();

          Mono<Void> historyInsert = insertHistory(events);
          Mono<Void> dbUpsert = upsertCurrentStateDB(latestEvent);
          Mono<Void> redisUpsert = upsertRedisState(latestEvent);

          return Mono.when(historyInsert, dbUpsert, redisUpsert)
              .doOnSuccess(v -> dailyCounter.increment(events.size()))
              .doOnError(e -> log.error("Telemetry batch failed", e));
        }).then();
  }


  private Mono<Void> insertHistory(List<MessageEvent> events) {
    return Flux.fromIterable(events).map(this::toHistoryEntity).collectList()
        .flatMapMany(historyPersistence::insertAll).then();
  }



  private Mono<Void> upsertCurrentStateDB(MessageEvent event) {
    return Mono.just(event).map(this::toCurrentEntity).flatMap(currentPersistence::upsert).then();
  }


  /**
   * Updates Redis and manages H3 cell transitions. Checks if the asset moved to a new hexagon and
   * cleans up the old cell.
   */
  private Mono<Void> upsertRedisState(MessageEvent event) {
    String assetKey = H3Constants.KEY_ASSET + event.getAssetId();

    // Getting H3 index of the resolution as a String
    // Convert lat/lon to H3 index as Long
    Long h3IndexLong = h3Service.toH3CellAddress(event.getLatitude(), event.getLongitude(),
        H3Constants.H3_RESOLUTION_8);

    // Convert H3 index to String for Redis storage
    String newH3 = h3IndexLong.toString();

    // Fetching current hash to check if the asset moved to a new hexagon
    return redisService.getHash(assetKey, Object.class).defaultIfEmpty(Map.of())
        .flatMap(oldState -> {
          // Reading the old H3 index from the previous state
          String oldH3 = (String) oldState.get("h3Index");

          // Mapping DTO to Map for Redis Hash storage
          Map<String, Object> fields = JsonMapperUtil.toMap(event);
          fields.put("h3Index", newH3);

          // Updating the Asset Snapshot in Redis
          Mono<Void> updateHash = redisService.putHash(assetKey, fields).then();

          // Handling H3 Set Membership (Move Logic)
          Mono<Void> cellMovement;
          if (oldH3 != null && !oldH3.equals(newH3)) {
            // Asset moved: Removing from old set, add to new set
            cellMovement = Mono.when(
                redisService.removeFromSet(H3Constants.KEY_H3 + oldH3,
                    event.getAssetId().toString()),
                redisService.addToSet(H3Constants.KEY_H3 + newH3, event.getAssetId().toString()));
          } else {
            // Asset in same cell (or new asset): Just ensure it's in the current set
            cellMovement = redisService
                .addToSet(H3Constants.KEY_H3 + newH3, event.getAssetId().toString()).then();
          }

          // Broadcast real-time update to the H3-specific channel
          Mono<Void> notify = redisService.publish(H3Constants.STREAM + newH3, fields).then();

          return Mono.when(updateHash, cellMovement, notify);
        });
  }



  private AssetLocationHistory toHistoryEntity(MessageEvent event) {
    return AssetLocationHistory.builder().assetId(event.getAssetId()).deviceTs(event.getDeviceTs())
        .latitude(event.getLatitude()).longitude(event.getLongitude())
        .location(Point.of(event.getLongitude(), event.getLatitude())).speed(event.getSpeed())
        .h3Index(h3Service.toH3CellAddress(event.getLatitude(), event.getLongitude(),
            H3Constants.H3_RESOLUTION_8))
        .speed(event.getSpeed()).heading(event.getHeading()).processedAt(Instant.now()).build();
  }

  private AssetCurrentLocation toCurrentEntity(MessageEvent event) {
    return AssetCurrentLocation.builder().assetId(event.getAssetId())
        .currentLat(event.getLatitude()).currentLon(event.getLongitude())
        .location(Point.of(event.getLongitude(), event.getLatitude())).speed(event.getSpeed())
        .heading(event.getHeading())
        .h3Index(h3Service.toH3CellAddress(event.getLatitude(), event.getLongitude(),
            H3Constants.H3_RESOLUTION_8))
        .deviceTs(event.getDeviceTs())
        .processedAt(event.getProcessedAt() != null ? event.getProcessedAt() : Instant.now())
        .build();
  }

}
