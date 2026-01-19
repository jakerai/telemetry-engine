package com.telemetry.engine.consumer.persistence;

import java.util.List;
import com.telemetry.engine.consumer.entity.AssetLocationHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetLocationHistoryPersistence {

  Mono<AssetLocationHistory> save(AssetLocationHistory assetLocationEvent);
  
  Flux<AssetLocationHistory> saveAll(List<AssetLocationHistory> assetLocationEvent);
  
}
