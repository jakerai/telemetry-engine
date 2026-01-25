package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.entity.AssetCategory;
import reactor.core.publisher.Mono;

public interface AssetCategoryPersistence {

  Mono<AssetCategory> save(AssetCategory assetCategory);
  
  Mono<Boolean> existsByName(String name);

  Mono<AssetCategory> findByName(String name);

}
