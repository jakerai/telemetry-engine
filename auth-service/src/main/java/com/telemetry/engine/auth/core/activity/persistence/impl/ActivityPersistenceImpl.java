package com.telemetry.engine.auth.core.activity.persistence.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.telemetry.engine.auth.core.activity.entity.Activity;
import com.telemetry.engine.auth.core.activity.persistence.ActivityPersistence;
import com.telemetry.engine.auth.core.activity.repository.ActivityRepository;
import com.telemetry.engine.common.exception.DataPersistenceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ActivityPersistenceImpl implements ActivityPersistence {

  @Autowired
  private ActivityRepository activityRepository;

  @Override
  public Activity save(Activity activity) {
    Assert.notNull(activity, "Activity must not be null");
    try {
      return activityRepository.save(activity);
    } catch (DataAccessException ex) {
      log.error("[ActivityLogPersistenceImpl.save] DB error while saving activityLog: user ID={}",
          activity.getUserId(), ex);
      throw new DataPersistenceException("Failed to save activity");
    }
  }

}
