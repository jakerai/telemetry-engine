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
 * Production-ready Redis writer for assets. Handles: - Move from old H3 to new H3 - TTL -
 * Idempotency - Pub/Sub - Async pipelining
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
   * Upsert asset state into Redis. Returns Mono<Void> which completes when all Redis ops succeed.
   */
  public Mono<Void> upsert(MessageEvent event) {
    String assetKey = AssetRedisKeys.assetState(event.getAssetId());
    String newH3 = h3Service.toH3(event.getLatitude(), event.getLongitude());

    return redisService.getValue(assetKey, AssetRedisState.class)
        .defaultIfEmpty(AssetRedisState.builder().build()).flatMap(oldState -> {

          Instant oldTs = oldState.getDeviceTs();
          Instant newTs = event.getDeviceTs();

          if (oldTs != null && newTs != null && !newTs.isAfter(oldTs)) {
            log.info("Skipping out-of-order update for assetId={} (oldTs={}, newTs={})",
                event.getAssetId(), oldTs, newTs);
            return Mono.empty();
          }

          boolean isNewAsset = oldTs == null;
          if (isNewAsset) {
            log.info("Adding new asset state for assetId={} at H3={}", event.getAssetId(), newH3);
          } else {
            log.info("Updating assetId={} from old H3={} to new H3={}", event.getAssetId(),
                oldState.getH3Index(), newH3);
          }

          // Prepare new state
          AssetRedisState newState = AssetRedisState.builder().assetId(event.getAssetId())
              .assetTypeId(event.getAssetTypeId()).latitude(event.getLatitude())
              .longitude(event.getLongitude()).speed(event.getSpeed()).heading(event.getHeading())
              .deviceTs(event.getDeviceTs()).processedAt(event.getProcessedAt()).h3Index(newH3)
              .build();

          // Keys
          String oldH3SetKey = oldState.getH3Index() != null
              ? AssetRedisKeys.h3TypeCell(oldState.getAssetTypeId(), oldState.getH3Index())
              : null;
          String newH3SetKey = AssetRedisKeys.h3TypeCell(newState.getAssetTypeId(), newH3);
          String channel = AssetRedisKeys.streamTypeCell(newState.getAssetTypeId(), newH3);

          // Reactive pipeline
          return Mono.when(
              // Remove from old H3 set if moved
              (oldState.getH3Index() != null && !Objects.equals(oldState.getH3Index(), newH3))
                  ? redisService.removeFromSet(oldH3SetKey, String.valueOf(newState.getAssetId()))
                      .doOnSuccess(v -> log.info("Removed assetId={} from old H3 set {}",
                          newState.getAssetId(), oldH3SetKey))
                  : Mono.empty(),

              // Add to new H3 set
              redisService.addToSet(newH3SetKey, String.valueOf(newState.getAssetId()))
                  .doOnSuccess(v -> log.info("Added assetId={} to new H3 set {}",
                      newState.getAssetId(), newH3SetKey)),

              // Save asset state with TTL
              redisService.putValue(assetKey, newState, Duration.ofSeconds(STATE_TTL_SECONDS))
                  .doOnSuccess(v -> log.info("Saved assetId={} state in Redis key={}",
                      newState.getAssetId(), assetKey)),

              // Publish update
              redisService.publish(channel, newState)
                  .doOnSuccess(v -> log.info("Published update for assetId={} to channel={}",
                      newState.getAssetId(), channel)));
        });
  }

}
