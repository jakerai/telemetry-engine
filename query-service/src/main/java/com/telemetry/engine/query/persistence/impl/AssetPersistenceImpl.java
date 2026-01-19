package com.telemetry.engine.query.persistence.impl;

import org.springframework.stereotype.Component;
import com.telemetry.engine.query.persistence.AssetPersistence;
import com.telemetry.engine.query.repository.AssetRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssetPersistenceImpl implements AssetPersistence {

  private final AssetRepository assetRepository;
  
  
}
