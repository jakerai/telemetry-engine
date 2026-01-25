package com.telemetry.engine.query.service.impl;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.redis.model.AssetRedisState;
import com.telemetry.engine.common.redis.reader.RedisAssetStateReader;
import com.telemetry.engine.query.dto.AssetDto;
import com.telemetry.engine.query.dto.response.AssetLocationStreamEvent;
import com.telemetry.engine.query.dto.response.AssetUpdateResponse;
import com.telemetry.engine.query.dto.response.NearbyAsset;
import com.telemetry.engine.query.dto.response.NearbyAssetStreamEvent;
import com.telemetry.engine.query.dto.response.PagedResponse;
import com.telemetry.engine.query.dto.resquest.AssetCreateRequest;
import com.telemetry.engine.query.dto.resquest.AssetFilter;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

  private final AssetCurrentLocationPersistence assetCurrentLocationPersistence;
  private final AssetPersistence assetPersistence;
  private final RedisAssetStateReader redisAssetStateReader;
  private final TransactionalOperator operator;

  @Override
  public Mono<ServiceResponse<Void>> createAsset(
      ServiceRequest<AssetCreateRequest> serviceRequest) {

    AssetCreateRequest createAsset = serviceRequest.payload();
    log.info("Creating asset with name={} for owner ID={}", createAsset.getName(),
        createAsset.getOwnerId());

    Asset asset = AssetCreateRequest.from(createAsset);

    return assetPersistence.save(asset).as(operator::transactional)
        .map(savedAsset -> ServiceResponse.<Void>success("Asset created successfully"))
        .onErrorResume(e -> Mono
            .just(ServiceResponse.<Void>error("Failed to create asset: " + e.getMessage())));
  }



  @Override
  public Mono<ServiceResponse<AssetDto>> getAsset(Long assetId) {
    return assetPersistence.findById(assetId).map(asset -> AssetDto.from(asset))
        .map(assetDto -> ServiceResponse.success(assetDto, "Asset fetched successfully"))
        .switchIfEmpty(Mono.just(ServiceResponse.error("Asset not found"))).onErrorResume(
            e -> Mono.just(ServiceResponse.error("Failed to fetch asset: " + e.getMessage())));
  }


  @Override
  public Mono<ServiceResponse<AssetUpdateResponse>> updateAsset(Long assetId,
      AssetUpdateRequest request) {

    return assetPersistence.findById(assetId).flatMap(existing -> {

      boolean changed = AssetUpdateRequest.applyIfChanged(existing, request);

      if (!changed) {
        return Mono.just(
            ServiceResponse.success(AssetUpdateResponse.from(existing), "No changes to update"));
      }

      return assetPersistence.save(existing).map(saved -> ServiceResponse
          .success(AssetUpdateResponse.from(saved), "Asset updated successfully"));
    });

  }



  @Override
  public Mono<ServiceResponse<PagedResponse<AssetDto>>> getAssets(int page, int size,
      String createdBy, String modifiedBy, Long typeId, Long ownerId, String category,
      String sortDirection, String sortBy) {

    int finalPage = Math.max(page, 0);
    int finalSize = Math.max(size, 1);

    page = Math.max(page, 0);
    size = Math.max(size, 1);

    sortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
    sortDirection =
        (sortDirection == null || sortDirection.isBlank()) ? "DESC" : sortDirection.toUpperCase();

    AssetFilter filter =
        AssetFilter.builder().createdBy(createdBy).modifiedBy(modifiedBy).typeId(typeId)
            .ownerId(ownerId).category(category).sortBy(sortBy).sortDir(sortDirection).build();

    return Mono
        .zip(assetPersistence.count(filter).defaultIfEmpty(0L),
            assetPersistence.findAll(filter, finalPage, finalSize).collectList()
                .defaultIfEmpty(Collections.emptyList()))
        .<ServiceResponse<PagedResponse<AssetDto>>>map(tuple -> {
          long total = tuple.getT1();
          List<AssetDto> data = tuple.getT2();

          int totalPages = (int) Math.ceil((double) total / finalSize);
          boolean hasNext = (finalPage + 1) < totalPages;

          return ServiceResponse.success(
              new PagedResponse<AssetDto>(data, finalPage, finalSize, total, totalPages, hasNext),
              "Fetched successfully");
        }).onErrorResume(e -> {
          log.error("Failed to fetch filtered assets", e);
          return Mono
              .just(ServiceResponse.<PagedResponse<AssetDto>>error("Failed to fetch assets"));
        });
  }

  /**
   * Fetches nearby assets (SNAPSHOT: all assets at that moment + DELTAS: new updates + HEARTBEAT)
   */
  public Flux<NearbyAssetStreamEvent> getNearbyAssets(double lat, double lon, Long assetTypeId,
      int radiusMeters) {

    NearbyAssetsRequest req = NearbyAssetsRequest.builder().assetTypeId(assetTypeId).lat(lat)
        .lon(lon).radiusMeters(radiusMeters).build();

    /* SNAPSHOT (finite) */
    Flux<NearbyAssetStreamEvent> snapshot = getSnapshot(req).flatMapMany(Flux::fromIterable)
        .map(NearbyAsset::from).map(resp -> NearbyAssetStreamEvent.builder()
            .streamType(StreamType.SNAPSHOT).data(resp).build());

    /* DELTAS (infinite) */
    Flux<NearbyAssetStreamEvent> deltas = subscribeToDeltas(req).map(NearbyAsset::from).map(
        resp -> NearbyAssetStreamEvent.builder().streamType(StreamType.DELTA).data(resp).build());

    /* HEARTBEAT (infinite) */
    Flux<NearbyAssetStreamEvent> heartbeat =
        Flux.interval(Duration.ofSeconds(15)).map(tick -> NearbyAssetStreamEvent.builder()
            .streamType(StreamType.HEARTBEAT).data(null).build());

    return Flux.concat(snapshot, Flux.merge(deltas, heartbeat));
  }



  /**
   * Fetches snapshot from Redis first else fallback to DB
   * 
   * @param req
   * @return List of nearby assets
   */
  private Mono<List<AssetRedisState>> getSnapshot(NearbyAssetsRequest req) {
    int ring = (int) Math.ceil(req.getRadiusMeters() / 1000.0);

    log.debug("Fetching snapshot for assetTypeId={} at lat={}, lon={}, radius={}",
        req.getAssetTypeId(), req.getLat(), req.getLon(), req.getRadiusMeters());

    /* Fetch from Redis */
    return redisAssetStateReader
        .findNearbyByType(req.getLat(), req.getLon(), req.getAssetTypeId(), ring).collectList()
        .flatMap(list -> {
          if (list.isEmpty()) {
            log.debug("No assets found in Redis, falling back to DB for assetTypeId={}",
                req.getAssetTypeId());

            /* Fetch from DB */
            return assetCurrentLocationPersistence
                .findNearby(req.getAssetTypeId(), req.getLat(), req.getLon(), req.getRadiusMeters())
                .map(dto -> {
                  log.debug("Adding asset {} from DB snapshot", dto.assetId());
                  return AssetRedisState.builder().assetId(dto.assetId())
                      .assetTypeId(dto.assetTypeId()).latitude(dto.latitude())
                      .longitude(dto.longitude()).heading(dto.heading()).speed(dto.speed())
                      .deviceTs(dto.deviceTs()).build();
                }).collectList();
          }

          log.debug("Found {} assets in Redis for snapshot", list.size());
          return Mono.just(list);
        });
  }


  /**
   * Subscribes to real-time deltas from Redis
   * 
   * @param req
   * @return
   */
  private Flux<AssetRedisState> subscribeToDeltas(NearbyAssetsRequest req) {

    int ring = (int) Math.ceil(req.getRadiusMeters() / 1000.0);
    return redisAssetStateReader.subscribeToNearbyTypeDeltas(req.getLat(), req.getLon(),
        req.getAssetTypeId(), ring);
  }



  @Override
  public Flux<AssetLocationStreamEvent> trackAsset(Long assetId) {
    /* subscribe to a single asset */
    Flux<AssetRedisState> stateFlux = redisAssetStateReader.streamAsset(assetId).publish()
        .refCount(1); /* Automatically unsubscribe when client disconnects */

    Flux<AssetLocationStreamEvent> updates =
        stateFlux.map(state -> AssetLocationStreamEvent.builder().streamType(StreamType.UPDATE)
            .data(NearbyAsset.from(state)).build()).publishOn(Schedulers.boundedElastic());

    /* Heartbeat every 10 seconds */
    Flux<AssetLocationStreamEvent> heartbeat =
        Flux.interval(Duration.ofSeconds(10)).map(tick -> AssetLocationStreamEvent.builder()
            .streamType(StreamType.HEARTBEAT).data(null).build());

    /* Emit OFFLINE if no updates in 60 seconds */
    Flux<AssetLocationStreamEvent> offline = updates.timeout(Duration.ofSeconds(60), Flux.just(
        AssetLocationStreamEvent.builder().streamType(StreamType.OFFLINE).data(null).build()));

    return Flux.merge(updates, heartbeat, offline);
  }


}
