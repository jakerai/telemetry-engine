package com.telemetry.engine.common.redis.reader;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import com.telemetry.engine.common.geo.H3Service;
import com.telemetry.engine.common.mapper.JsonMapperUtil;
import com.telemetry.engine.common.redis.RedisService;
import com.telemetry.engine.common.redis.key.AssetRedisKeys;
import com.telemetry.engine.common.redis.model.AssetRedisState;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Redis reader for querying assets by H3 + assetType.
 */
@Slf4j
public class RedisAssetStateReader {

  private final RedisService redisService;
  private final H3Service h3Service;

  public RedisAssetStateReader(RedisService redisService, H3Service h3Service) {
    this.redisService = redisService;
    this.h3Service = h3Service;
  }

  /**
   * Fetch a single asset state by asset ID.
   */
  public Mono<AssetRedisState> getAssetState(long assetId) {
    String key = AssetRedisKeys.assetState(assetId);
    return redisService.getValue(key, AssetRedisState.class).timeout(Duration.ofSeconds(1)) // safety
                                                                                            // timeout
        .onErrorResume(e -> Mono.empty());
  }

  /**
   * Find all assets nearby (H3 k-ring) for a given assetType.
   *
   * @param lat center latitude
   * @param lon center longitude
   * @param assetTypeId type filter
   * @param ring k-ring size
   * @return Flux of AssetRedisState
   */
  public Flux<AssetRedisState> findNearbyByType(double lat, double lon, long assetTypeId,
      int ring) {

    String centerH3 = h3Service.toH3(lat, lon);

    log.debug("findNearbyByType called: assetTypeId={}, lat={}, lon={}, ring={}, centerH3={}",
        assetTypeId, lat, lon, ring, centerH3);

    // Generate H3 keys for this type
    List<String> h3Keys = h3Service.kRing(centerH3, ring).stream()
        .map(h3 -> AssetRedisKeys.h3TypeCell(assetTypeId, h3)).collect(Collectors.toList());

    log.debug("Generated {} H3 Redis set keys for assetTypeId={}: {}", h3Keys.size(), assetTypeId,
        h3Keys);

    // Get all asset IDs in those H3 sets (SUNION)
    return redisService.unionSets(h3Keys)
        .doOnSubscribe(s -> log.debug("Executing Redis SUNION for assetTypeId={}", assetTypeId))
        .doOnNext(assetIdStr -> log.debug("SUNION returned assetId={}", assetIdStr))
        .flatMap(assetIdStr -> {
          long assetId;
          try {
            assetId = Long.parseLong(assetIdStr);
          } catch (NumberFormatException e) {
            log.warn("Invalid assetId '{}' found in Redis H3 set", assetIdStr);
            return Mono.empty();
          }

          log.debug("Fetching Redis state for assetId={}", assetId);
          return getAssetState(assetId);
        });
  }


  /**
   * Optional helper: fetch as CompletableFuture
   */
  public List<AssetRedisState> findNearbyByTypeCF(double lat, double lon, long assetTypeId,
      int ring) {
    return findNearbyByType(lat, lon, assetTypeId, ring).collectList().block(); // blocking for
                                                                                // legacy sync usage
  }

  /**
   * New method: subscribe to delta updates for a given assetType + nearby H3 ring
   */
  public Flux<AssetRedisState> subscribeToNearbyTypeDeltas(double lat, double lon, long assetTypeId,
      int ring) {
    String centerH3 = h3Service.toH3(lat, lon);
    List<String> h3Cells = h3Service.kRing(centerH3, ring);

    List<String> channels = h3Cells.stream()
        .map(h3 -> AssetRedisKeys.streamTypeCell(assetTypeId, h3)).collect(Collectors.toList());

    log.debug("Subscribing to Redis delta channels for assetTypeId={} channels={}", assetTypeId,
        channels);

    return Flux.fromIterable(channels).flatMap(redisService::subscribe) // returns Flux<String> JSON
                                                                        // updates
        .map(json -> {
          AssetRedisState state = JsonMapperUtil.deserializeFromJson(json, AssetRedisState.class);
          log.debug("Delta received for assetId={}, h3Index={}", state.getAssetId(),
              state.getH3Index());
          return state;
        }).publishOn(Schedulers.boundedElastic());

  }


  /**
   * Reactive subscriber for a single asset. Automatically emits updates and unsubscribes on client
   * disconnect.
   *
   * @param assetId the asset ID to track
   * @return Flux emitting updated AssetRedisState
   */
  public Flux<AssetRedisState> streamAsset(long assetId) {
    String channel = AssetRedisKeys.streamAsset(assetId);

    log.debug("Tracking assetId={} on channel={}", assetId, channel);

    // Get last known state from Redis to emit immediately on subscription
    Mono<AssetRedisState> lastKnownState = getAssetState(assetId)
        .doOnNext(state -> log.debug("Emitting last known state for assetId={}", assetId))
        .defaultIfEmpty(null);

    // Subscribe to live updates
    Flux<AssetRedisState> liveUpdates = redisService.subscribe(channel).map(json -> {
      try {
        return JsonMapperUtil.deserializeFromJson(json, AssetRedisState.class);
      } catch (Exception e) {
        log.error("Failed to deserialize asset update for assetId={}", assetId, e);
        return null;
      }
    }).filter(state -> state != null).publishOn(Schedulers.boundedElastic())
        .doOnCancel(() -> log.debug("Client disconnected, unsubscribed from assetId={}", assetId));

    // Emit last known state first, then live updates
    return lastKnownState.flatMapMany(state -> {
      if (state != null) {
        return Flux.concat(Mono.just(state), liveUpdates);
      } else {
        return liveUpdates;
      }
    });
  }


}
