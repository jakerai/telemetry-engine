package com.telemetry.engine.query.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.query.entity.AssetCategory;
import reactor.core.publisher.Mono;

@Repository
public interface AssetCategoryRepository
    extends ReactiveCrudRepository<AssetCategory, Long> {

  Mono<Boolean> existsByCode(String code);

  Mono<AssetCategory> findByCode(String code);
  
}
