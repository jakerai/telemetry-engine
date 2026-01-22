package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.dto.response.AssetLocation;
import com.telemetry.engine.query.entity.AssetCurrentLocation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetCurrentLocationPersistence {

  Flux<AssetLocation> findNearby(String assetType, double lat, double lon,
      double radiusMeters);

  Mono<AssetLocation> findByAssetId(Long assetId);

  Mono<AssetLocation> save(AssetCurrentLocation asset);

}
