package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.dto.AssetLocationViewDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetCurrentLocationPersistence {

  Flux<AssetLocationViewDto> findNearby(String assetType, double lat, double lon,
      double radiusMeters);

  Mono<AssetLocationViewDto> findByAssetId(Long assetId);

}
