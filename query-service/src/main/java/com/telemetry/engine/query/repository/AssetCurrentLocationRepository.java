package com.telemetry.engine.query.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetCurrentLocationRepository
    extends ReactiveCrudRepository<AssetCurrentLocationRepository, Long> {

}
