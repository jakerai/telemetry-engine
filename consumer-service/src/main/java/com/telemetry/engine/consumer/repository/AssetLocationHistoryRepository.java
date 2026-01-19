package com.telemetry.engine.consumer.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.consumer.entity.AssetLocationHistory;

@Repository
public interface AssetLocationHistoryRepository extends ReactiveCrudRepository<AssetLocationHistory, Long> {

}
