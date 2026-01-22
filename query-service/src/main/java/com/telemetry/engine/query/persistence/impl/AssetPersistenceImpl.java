package com.telemetry.engine.query.persistence.impl;

import org.springframework.stereotype.Component;
import com.telemetry.engine.query.entity.Asset;
import com.telemetry.engine.query.persistence.AssetPersistence;
import com.telemetry.engine.query.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetPersistenceImpl implements AssetPersistence {

  private final AssetRepository assetRepository;

  @Override
  public Mono<Asset> save(Asset asset) {
    return assetRepository.save(asset);
  }


}
