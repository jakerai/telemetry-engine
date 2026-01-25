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
  public Mono<AssetType> save(AssetType assetType) {
    return assetTypeRepository.save(assetType);
  }

  @Override
  public Mono<Boolean> existsByName(String name) {
    return assetTypeRepository.existsByName(name);
  }

  @Override
  public Mono<AssetType> findByName(String name) {
    return assetTypeRepository.findByName(name);
  }

  @Override
  public Mono<Long> findCategoryIdByName(String name) {
   
    return  assetTypeRepository.findCategoryIdByName(name);
  }

}
