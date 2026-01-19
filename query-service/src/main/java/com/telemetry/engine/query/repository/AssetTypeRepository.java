package com.telemetry.engine.query.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.query.entity.AssetType;
import reactor.core.publisher.Mono;

@Repository
public interface AssetTypeRepository extends ReactiveCrudRepository<AssetType, Long> {

  Mono<Boolean> existsByCode(String code);

  Mono<AssetType> findByCode(String code);

  Mono<Long> findCategoryIdByCode(String code);

}
