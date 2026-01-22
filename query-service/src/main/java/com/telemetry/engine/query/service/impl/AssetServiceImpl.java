package com.telemetry.engine.query.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.telemetry.engine.common.constansts.H3Constants;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.geo.H3Service;
import com.telemetry.engine.common.mapper.JsonMapperUtil;
import com.telemetry.engine.common.redis.RedisService;
import com.telemetry.engine.query.dto.AssetDto;
import com.telemetry.engine.query.dto.response.AssetLocation;
import com.telemetry.engine.query.dto.response.AssetLocationStreamEvent;
import com.telemetry.engine.query.dto.response.AssetUpdateResponse;
import com.telemetry.engine.query.dto.response.NearbyAsset;
import com.telemetry.engine.query.dto.response.NearbyAssetStreamEvent;
import com.telemetry.engine.query.dto.response.PagedResponse;
import com.telemetry.engine.query.dto.resquest.AssetCreateRequest;
import com.telemetry.engine.query.dto.resquest.AssetUpdateRequest;
import com.telemetry.engine.query.dto.resquest.NearbyAssetsRequest;
import com.telemetry.engine.query.entity.Asset;
import com.telemetry.engine.query.enums.StreamType;
import com.telemetry.engine.query.persistence.AssetCurrentLocationPersistence;
import com.telemetry.engine.query.persistence.AssetPersistence;
import com.telemetry.engine.query.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

  private final AssetCurrentLocationPersistence assetCurrentLocationPersistence;
  private final AssetPersistence assetPersistence;
  private final RedisService redisService;
  private final H3Service h3Service;

  @Override
  public Mono<ServiceResponse<Void>> createAsset(
      ServiceRequest<AssetCreateRequest> serviceRequest) {

    AssetCreateRequest createAsset = serviceRequest.payload();
    Asset asset = AssetCreateRequest.from(createAsset);

    return assetPersistence.save(asset)
        .map(savedAsset -> ServiceResponse.<Void>success("Asset created successfully"))
        .onErrorResume(e -> Mono
            .just(ServiceResponse.<Void>error("Failed to create asset: " + e.getMessage())));
  }



  @Override
  public Mono<ServiceResponse<AssetDto>> getAsset(Long assetId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Mono<ServiceResponse<AssetUpdateResponse>> updateAsset(Long assetId,
      AssetUpdateRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Mono<ServiceResponse<PagedResponse<AssetDto>>> getAssets(int page, int size) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Mono<ServiceResponse<PagedResponse<AssetDto>>> getAssetsByOwnerId(Long ownerId, int page,
      int size) {
    // TODO Auto-generated method stub
    return null;
  }



  /**
   * Transport-agnostic: returns a Flux of asset maps (snapshot + deltas + heartbeat)
   */
  @Override
  public Flux<NearbyAssetStreamEvent> getNearbyAssets(double lat, double lon, String assetType,
      int radius) {
    NearbyAssetsRequest req = NearbyAssetsRequest.builder().assetType(assetType).lat(lat).lon(lon)
        .radiusMeters(radius).build();

    /* SNAPSHOT (finite) */
    Flux<NearbyAssetStreamEvent> snapshot = getSnapshot(req).flatMapMany(Flux::fromIterable)
        .map(this::toNearbyResponse).map(resp -> NearbyAssetStreamEvent.builder()
            .streamType(StreamType.SNAPSHOT).data(resp).build());

    /* DELTAS (infinite) */
    Flux<NearbyAssetStreamEvent> deltas = subscribeToDeltas(req).map(this::toNearbyResponse).map(
        resp -> NearbyAssetStreamEvent.builder().streamType(StreamType.DELTA).data(resp).build());

    /* HEARTBEAT (infinite) */
    Flux<NearbyAssetStreamEvent> heartbeat =
        Flux.interval(Duration.ofSeconds(15)).map(tick -> NearbyAssetStreamEvent.builder()
            .streamType(StreamType.HEARTBEAT).data(null).build());

    return Flux.concat(snapshot, Flux.merge(deltas, heartbeat));
  }

  /**
   * Fetch snapshot from Redis first; fallback to DB
   */
  private Mono<List<Map<String, Object>>> getSnapshot(NearbyAssetsRequest req) {

    Long h3IndexLong =
        h3Service.toH3CellAddress(req.getLat(), req.getLon(), H3Constants.H3_RESOLUTION_8);
    String centerH3 = h3IndexLong.toString();

    List<String> h3Keys = h3Service.kRing(centerH3, req.getRadiusMeters()).stream()
        .map(h -> H3Constants.KEY_H3 + h).collect(Collectors.toList());

    return redisService.unionSets(h3Keys).collectList().flatMap(assetIds -> {
      if (assetIds.isEmpty()) {
        // fallback to DB
        return assetCurrentLocationPersistence
            .findNearby(req.getAssetType(), req.getLat(), req.getLon(), req.getRadiusMeters())
            .map(dto -> JsonMapperUtil.toMap(dto)).collectList();
      }
      // fetching Redis hashes
      return Flux
          .fromIterable(assetIds).flatMap(id -> redisService
              .getHash(H3Constants.KEY_ASSET + id, Object.class).defaultIfEmpty(Map.of()))
          .filter(map -> !map.isEmpty()).collectList();
    });
  }


  /**
   * Subscribe to real-time deltas from Redis
   */
  private Flux<Map<String, Object>> subscribeToDeltas(NearbyAssetsRequest req) {

    Long h3IndexLong =
        h3Service.toH3CellAddress(req.getLat(), req.getLon(), H3Constants.H3_RESOLUTION_8);
    String centerH3 = h3IndexLong.toString();

    int ringSize = (int) Math.ceil(req.getRadiusMeters() / 1000.0);

    List<String> topics = h3Service.kRing(centerH3, ringSize).stream()
        .map(h -> H3Constants.STREAM + h).collect(Collectors.toList());

    return Flux.fromIterable(topics).flatMap(redisService::subscribe) // or listen/subscribe
                                                                      // depending on your
                                                                      // RedisService
        .publishOn(Schedulers.boundedElastic()) // offload CPU-bound JSON work
        .map(json -> {
          Map<String, Object> map =
              JsonMapperUtil.deserializeFromJson(json, new TypeReference<Map<String, Object>>() {}); // correct
          // overload
          return map != null ? map : Map.of();
        });
  }

  private NearbyAsset toNearbyResponse(Map<String, Object> map) {

    double speed = getDouble(map, "speed");

    return NearbyAsset.builder().assetId(getLong(map, "assetId"))
        .operatorId(getLong(map, "operatorId")).latitude(getDouble(map, "lat"))
        .longitude(getDouble(map, "lon")).distanceMeters(getDouble(map, "distanceMeters"))
        .moving(speed > 0).speed(speed).heading(getDouble(map, "heading"))
        .deviceTs(Instant.ofEpochMilli(getLong(map, "deviceTs"))).build();
  }

  private Long getLong(Map<String, Object> map, String key) {
    return map.containsKey(key) ? Long.parseLong(map.get(key).toString()) : null;
  }

  private double getDouble(Map<String, Object> map, String key) {
    return map.containsKey(key) ? Double.parseDouble(map.get(key).toString()) : 0.0;
  }

  @Override
  public Flux<AssetLocationStreamEvent> streamAssetCurrentLocation(Long assetId) {
    // Subscribe to Redis channel for this asset
    Flux<AssetLocationStreamEvent> updates = redisService.subscribe("stream:asset:" + assetId)
        .map(json -> AssetLocationStreamEvent.builder().streamType(StreamType.UPDATE)
            .data(JsonMapperUtil.deserializeFromJson(json, AssetLocation.class)).build());

    // Heartbeat
    Flux<AssetLocationStreamEvent> heartbeat =
        Flux.interval(Duration.ofSeconds(10)).map(t -> AssetLocationStreamEvent.builder()
            .streamType(StreamType.HEARTBEAT).data(null).build());

    // Offline detection: if no updates for 30s
    Flux<AssetLocationStreamEvent> offline = updates.timeout(Duration.ofSeconds(30)).onErrorReturn(
        AssetLocationStreamEvent.builder().streamType(StreamType.OFFLINE).data(null).build());

    return Flux.merge(updates, heartbeat, offline);
  }



}
