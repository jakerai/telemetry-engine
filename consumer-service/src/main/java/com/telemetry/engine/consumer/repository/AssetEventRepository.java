package com.telemetry.engine.consumer.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.consumer.entity.AssetEvent;

@Repository
public interface AssetEventRepository extends ReactiveCrudRepository<AssetEvent, Long> {

}
