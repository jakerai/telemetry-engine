package com.telemetry.engine.common.redis.writer;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import com.telemetry.engine.common.dto.MessageEvent;
import com.telemetry.engine.common.geo.H3Service;
import com.telemetry.engine.common.redis.RedisService;
import com.telemetry.engine.common.redis.key.AssetRedisKeys;
import com.telemetry.engine.common.redis.model.AssetRedisState;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Vishal Rai
 */


@Slf4j
public class RedisAssetStateWriter {

  private static final long STATE_TTL_SECONDS = 240L;

  private final RedisService redisService;
  private final H3Service h3Service;

  public RedisAssetStateWriter(RedisService redisService, H3Service h3Service) {
    this.redisService = redisService;
    this.h3Service = h3Service;
  }

  /**
   * Inserts or updates the Redis state for an incoming asset telemetry event.
   * <p>
   * This method is responsible for keeping Redis in sync with the latest known state of an asset
   * and maintaining geo-based indexes used for nearby-asset queries and real-time streaming.
   * </p>
   */
  public Mono<Void> upsert(MessageEvent event) {
    /* Redis key that stores the latest known state of this asset */
    String assetKey = AssetRedisKeys.keyForAssetState(event.getAssetId());

    /* Converting lat/lon into an H3 cell so we can index the asset spatially */
    String newH3 = h3Service.getH3CellAddress(event.getLatitude(), event.getLongitude());

    /* Fetch if this asset was seen before, else start with an empty state */
    return redisService.getValue(assetKey, AssetRedisState.class)
        .defaultIfEmpty(AssetRedisState.builder().build()).flatMap(oldState -> {

          Instant oldTs = oldState.getDeviceTs();
          Instant newTs = event.getDeviceTs();
          /*
           * Guarding against out-of-order events. If the incoming event is older or same time than
           * what we already have, we skip it to avoid overwriting newer state with stale data.
           */
          if (oldTs != null && newTs != null && !newTs.isAfter(oldTs)) {
            log.info("Skipping out-of-order update for assetId={} (oldTs={}, newTs={})",
                event.getAssetId(), oldTs, newTs);
            return Mono.empty();
          }

          /* If there was no previous timestamp, this is the first time we see this asset */
          boolean isNewAsset = oldTs == null;
          if (isNewAsset) {
            log.info("Adding new asset state for assetId={} at H3={}", event.getAssetId(), newH3);
          } else {
            log.info("Updating assetId={} from old H3={} to new H3={}", event.getAssetId(),
                oldState.getH3Index(), newH3);
          }

          /*
           * Building the new state that will replace the old one in Redis. This always represents
           * the latest known position and movement data.
           */
          AssetRedisState newState = AssetRedisState.builder().assetId(event.getAssetId())
              .assetTypeId(event.getAssetTypeId()).latitude(event.getLatitude())
              .longitude(event.getLongitude()).speed(event.getSpeed()).heading(event.getHeading())
              .deviceTs(event.getDeviceTs()).processedAt(event.getProcessedAt()).h3Index(newH3)
              .build();

          /* Redis set key for the old H3 cell (used only if the asset already existed) */
          String oldH3SetKey = oldState.getH3Index() != null
              ? AssetRedisKeys.keyForH3TypeCell(oldState.getAssetTypeId(), oldState.getH3Index())
              : null;

          /* Redis set key for the new H3 cell where the asset currently belongs */
          String newH3SetKey = AssetRedisKeys.keyForH3TypeCell(newState.getAssetTypeId(), newH3);

          /*
           * Stream / PubSub channel used for notifying subscribers interested in this asset type
           * within this H3 cell
           */
          String nearbychannel =
              AssetRedisKeys.keyForStreamTypeCell(newState.getAssetTypeId(), newH3);
          String assetChannel = AssetRedisKeys.keyForStreamAsset(newState.getAssetId());
          /*
           * Executing all Redis mutations together. Mono.when allows these independent operations
           * to run concurrently, keeping latency low while maintaining logical consistency.
           */
          return Mono.when(
              /*
               * If the asset moved to a different H3 cell, remove it from the old spatial index so
               * geo queries stay accurate
               */
              (oldState.getH3Index() != null && !Objects.equals(oldState.getH3Index(), newH3))
                  ? redisService.removeFromSet(oldH3SetKey, String.valueOf(newState.getAssetId()))
                      .doOnSuccess(v -> log.info("Removed assetId={} from old H3 set {}",
                          newState.getAssetId(), oldH3SetKey))
                  : Mono.empty(),

              /* Adding or re-adding the asset to the spatial index of its current H3 cell */
              redisService.addToSet(newH3SetKey, String.valueOf(newState.getAssetId()))
                  .doOnSuccess(v -> log.info("Added assetId={} to new H3 set {}",
                      newState.getAssetId(), newH3SetKey)),

              /*
               * Persisting the latest asset state with a TTL so stale assets naturally expire if
               * updates stop coming
               */
              redisService.putValue(assetKey, newState, Duration.ofSeconds(STATE_TTL_SECONDS))
                  .doOnSuccess(v -> log.info("Saved assetId={} state in Redis key={}",
                      newState.getAssetId(), assetKey)),

              /*
               * Publishing the update so real-time subscribers (nearby search, live tracking) get
               * notified immediately
               */
              redisService.publish(nearbychannel, newState)
                  .doOnSuccess(v -> log.info("Published update for assetId={} to nearby channel={}",
                      newState.getAssetId(), nearbychannel)),
              /* Publishing to per-asset channel for delta */
              redisService.publish(assetChannel, newState)
                  .doOnSuccess(v -> log.info("Published update for assetId={} to asset channel={}",
                      newState.getAssetId(), assetChannel)));

        });
  }

}
