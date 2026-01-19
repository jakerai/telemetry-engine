package com.telemetry.engine.query.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.query.entity.AssetLocationHistory;

@Repository
public interface AssetLocationHistoryRepository extends ReactiveCrudRepository<AssetLocationHistory, Long> {

}
