package com.telemetry.engine.consumer.persistence.impl;

import java.util.List;
import org.springframework.stereotype.Component;
import com.telemetry.engine.consumer.entity.AssetLocationHistory;
import com.telemetry.engine.consumer.persistence.AssetLocationHistoryPersistence;
import com.telemetry.engine.consumer.repository.AssetLocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class AssetLocationHistoryPersistenceImpl implements AssetLocationHistoryPersistence {

  private final AssetLocationHistoryRepository assetLocationEventRepository;

  @Override
  public Mono<AssetLocationHistory> save(AssetLocationHistory assetLocationEvent) {

    return assetLocationEventRepository.save(assetLocationEvent);
  }

  @Override
  public Flux<AssetLocationHistory> saveAll(List<AssetLocationHistory> events) {
    if (events == null || events.isEmpty()) {
      return Flux.empty();
    }

    /*
     * Spring Data R2DBC returns Flux<AssetLocationEvent> This allows the consumer to handle each
     * saved record as it completes
     */
    return assetLocationEventRepository.saveAll(events);
  }

}
