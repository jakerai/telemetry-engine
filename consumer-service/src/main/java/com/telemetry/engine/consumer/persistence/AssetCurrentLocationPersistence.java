package com.telemetry.engine.consumer.persistence;

import com.telemetry.engine.consumer.entity.AssetCurrentLocation;
import reactor.core.publisher.Mono;

public interface AssetCurrentLocationPersistence {

  Mono<Void> upsert(AssetCurrentLocation loc);
  
}
