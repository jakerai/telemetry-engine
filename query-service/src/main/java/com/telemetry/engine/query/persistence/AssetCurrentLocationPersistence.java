package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.dto.response.NearbyAsset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetCurrentLocationPersistence {

  Flux<NearbyAsset> findNearby(Long assetTypeId, double lat, double lon,
      double radiusMeters);

  Mono<NearbyAsset> findByAssetId(Long assetId);

}
