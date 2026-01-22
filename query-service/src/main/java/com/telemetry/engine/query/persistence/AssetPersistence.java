package com.telemetry.engine.query.persistence;

import com.telemetry.engine.query.entity.Asset;
import reactor.core.publisher.Mono;

public interface AssetPersistence {

  Mono<Asset> save(Asset asset);

}
