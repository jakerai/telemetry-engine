package com.telemetry.engine.query.persistence.impl;

import org.springframework.stereotype.Component;
import com.telemetry.engine.query.entity.AssetCategory;
import com.telemetry.engine.query.persistence.AssetCategoryPersistence;
import com.telemetry.engine.query.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetCategoryPersistenceImpl implements AssetCategoryPersistence {

  private final AssetCategoryRepository assetCategoryRepository;

  @Override
  public Mono<AssetCategory> save(AssetCategory assetCategory) {
    return assetCategoryRepository.save(assetCategory);
  }

  @Override
  public Mono<Boolean> existsByCode(String code) {
    return assetCategoryRepository.existsByCode(code);
  }

  @Override
  public Mono<AssetCategory> findByCode(String code) {
    return assetCategoryRepository.findByCode(code);
  }


}
