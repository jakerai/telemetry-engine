package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.entity.AssetType;
import reactor.core.publisher.Mono;

public interface AssetTypePersistence {
  
  Mono<AssetType> save(AssetType type);

  Mono<Boolean> existsByCode(String code);

  Mono<AssetType> findByCode(String code);

  Mono<Long> findCategoryIdByCode(String code);
  
}
