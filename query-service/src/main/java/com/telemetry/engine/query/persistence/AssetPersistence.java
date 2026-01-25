package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.dto.AssetDto;
import com.telemetry.engine.query.dto.resquest.AssetFilter;
import com.telemetry.engine.query.entity.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetPersistence {

  Mono<Asset> save(Asset asset);

  Mono<Asset> findById(Long assetId);

  Mono<Long> count(AssetFilter filter);

  Flux<AssetDto> findAll(AssetFilter filter, int page, int size);

}
