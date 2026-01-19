package com.telemetry.engine.query.persistence.impl;

import org.springframework.stereotype.Component;
import com.telemetry.engine.query.entity.AssetType;
import com.telemetry.engine.query.persistence.AssetTypePersistence;
import com.telemetry.engine.query.repository.AssetTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetTypePersistenceImpl implements AssetTypePersistence {

  private final AssetTypeRepository assetTypeRepository;

  @Override
  public Mono<AssetType> save(AssetType type) {
    return assetTypeRepository.save(type);
  }

  @Override
  public Mono<Boolean> existsByCode(String code) {
    return assetTypeRepository.existsByCode(code);
  }

  @Override
  public Mono<AssetType> findByCode(String code) {
    return assetTypeRepository.findByCode(code);
  }

  @Override
  public Mono<Long> findCategoryIdByCode(String code) {
   
    return  assetTypeRepository.findCategoryIdByCode(code);
  }

}
