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
 * @author Vishal Rai
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
   * Fetches a single asset state by asset ID.
   */
  public Mono<AssetRedisState> getAssetState(long assetId) {
    String key = AssetRedisKeys.keyForAssetState(assetId);
    return redisService.getValue(key, AssetRedisState.class).timeout(Duration.ofSeconds(1))
        .onErrorResume(e -> Mono.empty());
  }

  /**
   * Finds all assets nearby (H3 k-ring) for a given assetType.
   *
   * @param lat center latitude
   * @param lon center longitude
   * @param assetTypeId type filter
   * @param ring k-ring size
   * @return Flux of AssetRedisState
   */
  public Flux<AssetRedisState> findNearbyByType(double lat, double lon, long assetTypeId,
      int ring) {

    /* Converting the search center into an H3 cell so we can do spatial lookup */
    String centerH3 = h3Service.getH3CellAddress(lat, lon);

    log.debug("Fetching nearby assets: assetTypeId={}, lat={}, lon={}, ring={}, centerH3={}",
        assetTypeId, lat, lon, ring, centerH3);

    /*
     * Generating Redis set keys for all H3 cells in the k-ring around the center. Each set
     * represents assets of a given type inside one H3 cell.
     */
    List<String> h3Keys = h3Service.getKRingAddresses(centerH3, ring).stream()
        .map(h3 -> AssetRedisKeys.keyForH3TypeCell(assetTypeId, h3)).collect(Collectors.toList());

    log.debug("Generated {} H3 Redis set keys for assetTypeId={}: {}", h3Keys.size(), assetTypeId,
        h3Keys);

    /*
     * Performing a Redis SUNION across all H3 sets. This gives a de-duplicated list of assetIds
     * that fall within the requested radius (center cell + surrounding rings).
     */
    return redisService.unionSets(h3Keys)
        .doOnSubscribe(s -> log.debug("Executing Redis SUNION for assetTypeId={}", assetTypeId))
        .doOnNext(assetIdStr -> log.debug("SUNION returned assetId={}", assetIdStr))
        .flatMap(assetIdStr -> {
          /*
           * Asset IDs are stored as strings in Redis sets, so we need to convert them back to long
           * before lookup.
           */
          long assetId;
          try {
            assetId = Long.parseLong(assetIdStr);
          } catch (NumberFormatException e) {
            log.warn("Invalid assetId '{}' found in Redis H3 set", assetIdStr);
            return Mono.empty();
          }

          /*
           * Fetch the latest known state for this asset. This ensures we return fresh,
           * authoritative data instead of relying on index membership alone.
           */
          log.debug("Fetching Redis state for assetId={}", assetId);
          return getAssetState(assetId);
        });
  }


  /**
   * Optional helper to fetch as CompletableFuture
   */
  public List<AssetRedisState> findNearbyByTypeCF(double lat, double lon, long assetTypeId,
      int ring) {
    return findNearbyByType(lat, lon, assetTypeId, ring).collectList()
        .block(); /* blocking for legacy sync usage */
  }


  /**
   * Subscribes to real-time updates (deltas) for all assets of a given type within a specified H3
   * ring around a geographic point. This method is used for live tracking scenarios, where clients
   * need instant updates for nearby assets instead of polling Redis repeatedly.
   * <p>
   * <b>Notes:</b>
   * </p>
   * <ul>
   * <li>The returned Flux will continuously emit updates as new telemetry arrives.</li>
   * <li>It only streams assets currently in the H3 cells included in the ring.</li>
   * <li>Delta messages must be serialized to JSON when published by upstream code.</li>
   * </ul>
   *
   * @param lat latitude of the center point
   * @param lon longitude of the center point
   * @param assetTypeId asset type to filter
   * @param ring number of H3 rings around the center cell
   * @return a {@link Flux} of {@link AssetRedisState} representing live updates
   */
  public Flux<AssetRedisState> subscribeToNearbyTypeDeltas(double lat, double lon, long assetTypeId,
      int ring) {

    /* Converting lat/lon to the center H3 cell for spatial indexing */
    String centerH3 = h3Service.getH3CellAddress(lat, lon);

    /* Computing all neighboring H3 cells within the requested k-ring */
    List<String> h3Cells = h3Service.getKRingAddresses(centerH3, ring);

    /*
     * Generating the Redis Pub/Sub channels for this asset type + H3 cells Each H3 cell + asset
     * type combination has its own stream channel
     */
    List<String> channels =
        h3Cells.stream().map(h3 -> AssetRedisKeys.keyForStreamTypeCell(assetTypeId, h3))
            .collect(Collectors.toList());

    /*
     * Subscribing to all channels concurrently. redisService.subscribe returns a Flux<String>
     * containing JSON updates
     */
    log.debug("Subscribing to Redis delta channels for assetTypeId={} channels={}", assetTypeId,
        channels);
    return Flux.fromIterable(channels).flatMap(redisService::subscribe).map(json -> {
      AssetRedisState state = JsonMapperUtil.deserializeFromJson(json, AssetRedisState.class);
      log.debug("Delta received for assetId={}, h3Index={}", state.getAssetId(),
          state.getH3Index());
      return state;
    })/* Using boundedElastic scheduler for deserialization to avoid blocking reactive threads */
        .publishOn(Schedulers.boundedElastic());

  }


  /**
   * Fetch the last known state of an asset from Redis. Returns empty if no state exists.
   *
   * @param assetId the asset ID
   */
  public Mono<AssetRedisState> getLastSnapshotForAsset(long assetId) {
    return getAssetState(assetId) /* Fetch last known data */
        .doOnNext(state -> log.debug("Fetched last known snapshot for assetId={}", assetId))
        .switchIfEmpty(
            Mono.fromRunnable(() -> log.debug("No last known snapshot for assetId={}", assetId)));
  }


  /**
   * Subscribe to live delta updates for a specific asset from Redis Pub/Sub. Each update is
   * deserialized into AssetRedisState.
   *
   * @param assetId the asset ID
   */
  public Flux<AssetRedisState> subscribeToAssetDeltas(long assetId) {
    String channel = AssetRedisKeys.keyForStreamAsset(assetId);

    // This log will only print when someone subscribes
    return Flux.defer(() -> {
        log.info("Subscribing to asset delta channel={}", channel);

        return redisService.subscribe(channel)
            .publishOn(Schedulers.boundedElastic())
            .mapNotNull(json -> {
                try {
                    return JsonMapperUtil.deserializeFromJson(json, AssetRedisState.class);
                } catch (Exception e) {
                    log.error("Failed to deserialize delta for assetId={}", assetId, e);
                    return null; // skip invalid messages
                }
            })
            .doOnCancel(() -> log.debug("Client unsubscribed from assetId={}", assetId))
            /* Share subscription among multiple clients */
            .share();
    });
}



}
