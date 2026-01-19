package com.telemetry.engine.query.persistence.impl;

import org.springframework.stereotype.Component;
import com.telemetry.engine.query.repository.AssetLocationHistoryRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssetLocationHistoryPersistenceImpl {

  private final AssetLocationHistoryRepository assetLocationEventRepository;
  
}
