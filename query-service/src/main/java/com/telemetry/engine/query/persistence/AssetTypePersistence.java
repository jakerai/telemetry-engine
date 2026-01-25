package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.entity.AssetType;
import reactor.core.publisher.Mono;

public interface AssetTypePersistence {

  Mono<AssetType> save(AssetType assetType);

  Mono<Boolean> existsByName(String name);

  Mono<AssetType> findByName(String name);

  Mono<Long> findCategoryIdByName(String name);

}
