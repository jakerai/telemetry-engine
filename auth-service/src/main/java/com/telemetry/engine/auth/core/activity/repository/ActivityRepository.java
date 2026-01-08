package com.telemetry.engine.auth.core.activity.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.activity.entity.Activity;

@Repository
public interface ActivityRepository extends CrudRepository<Activity, Long> {

}
