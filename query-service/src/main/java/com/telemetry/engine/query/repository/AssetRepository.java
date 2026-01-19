package com.telemetry.engine.query.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.query.entity.Asset;


@Repository
public interface AssetRepository extends ReactiveCrudRepository<Asset, Long> {

}
