package com.telemetry.engine.query.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.telemetry.engine.common.constansts.H3Constants;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.geo.H3Service;
import com.telemetry.engine.common.mapper.MapperService;
import com.telemetry.engine.common.redis.RedisService;
import com.telemetry.engine.query.dto.AssetLocationViewDto;
import com.telemetry.engine.query.dto.NearbyAssetsRequest;
import com.telemetry.engine.query.persistence.AssetCurrentLocationPersistence;
import com.telemetry.engine.query.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {


  private final AssetCurrentLocationPersistence persistence;
  private final RedisService redisService;
  private final H3Service h3Service;
  private final MapperService mapperService;



  /**
   * Transport-agnostic: returns a Flux of asset maps (snapshot + deltas + heartbeat)
   */
  @Override
  public Flux<Map<String, Object>> getNearbyAssets(double lat, double lon, String assetType,
      int radius) {
    NearbyAssetsRequest req = NearbyAssetsRequest.builder().assetType(assetType).lat(lat).lon(lon)
        .radiusMeters(radius).build();

    /* Finite stream */
    Flux<Map<String, Object>> snapshot =
        getSnapshot(req).flatMapMany(Flux::fromIterable).map(map -> {
          map.put("streamType", "snapshot");
          return map;
        });

    /* Infinite stream */
    Flux<Map<String, Object>> deltas = subscribeToDeltas(req).map(map -> {
      map.put("streamType", "delta");
      return map;
    });

    /* Infinite stream */
    Flux<Map<String, Object>> heartbeat =
        Flux.interval(Duration.ofSeconds(15)).map(tick -> Map.of("streamType", "heartbeat"));

    /* Snapshot FIRST, then BOTH deltas and heartbeat together */
    return Flux.concat(snapshot, Flux.merge(deltas, heartbeat));
  }

  /**
   * Fetch snapshot from Redis first; fallback to DB
   */
  private Mono<List<Map<String, Object>>> getSnapshot(NearbyAssetsRequest req) {

    String centerH3 =
        h3Service.toH3CellAddress(req.getLat(), req.getLon(), H3Constants.H3_RESOLUTION_8);

    List<String> h3Keys = h3Service.kRing(centerH3, req.getRadiusMeters()).stream()
        .map(h -> H3Constants.KEY_H3 + h).collect(Collectors.toList());

    return redisService.unionSets(h3Keys).collectList().flatMap(assetIds -> {
      if (assetIds.isEmpty()) {
        // fallback to DB
        return persistence
            .findNearby(req.getAssetType(), req.getLat(), req.getLon(), req.getRadiusMeters())
            .map(dto -> mapperService.toMap(dto)).collectList();
      }
      // fetching Redis hashes
      return Flux.fromIterable(assetIds)
          .flatMap(id -> redisService.getHash(H3Constants.KEY_ASSET + id).defaultIfEmpty(Map.of()))
          .filter(map -> !map.isEmpty()).collectList();
    });
  }

  /**
   * Subscribe to real-time deltas from Redis
   */
  private Flux<Map<String, Object>> subscribeToDeltas(NearbyAssetsRequest req) {

    String centerH3 =
        h3Service.toH3CellAddress(req.getLat(), req.getLon(), H3Constants.H3_RESOLUTION_8);
    int ringSize = (int) Math.ceil(req.getRadiusMeters() / 1000.0);

    List<String> topics = h3Service.kRing(centerH3, ringSize).stream()
        .map(h -> H3Constants.STREAM + h).collect(Collectors.toList());

    return Flux.fromIterable(topics).flatMap(redisService::subscribeToChannel).map(json -> {
      Map<String, Object> map =
          mapperService.deserializeFromJson(json, new TypeReference<Map<String, Object>>() {});
      return map != null ? map : Map.of();
    });
  }

  @Override
  public Flux<ServiceResponse<AssetLocationViewDto>> streamAssetCurrentLocation(Long assetId) {
    String topic = H3Constants.STREAM + "asset:" + assetId;

    return null;
  }



}
