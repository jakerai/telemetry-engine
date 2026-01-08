package com.telemetry.engine.auth.core.activity.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.activity.entity.Activity;
import com.telemetry.engine.auth.core.activity.enums.Action;
import com.telemetry.engine.auth.core.activity.persistence.ActivityPersistence;
import com.telemetry.engine.auth.core.activity.service.ActivityService;


@Service
public class ActivityServiceImpl implements ActivityService {
  private static final Logger log = LoggerFactory.getLogger(ActivityServiceImpl.class);

  @Autowired
  private ActivityPersistence activityLogPersistence;

  @Override
  public void logActivity(Action action, Long userId, String ip, String remarks) {
    try {
      Activity activitylog =
          Activity.builder().action(action).userId(userId).ip(ip).remarks(remarks).build();

      activityLogPersistence.save(activitylog);

      log.info("User Activity Logged: action={}, userId={}, ip={}, remarks={}", action, userId, ip,
          remarks);
    } catch (Exception ex) {
      log.error("Failed to log user activity: action={}, userId={}, ip={}, remarks={}, error={}",
          action, userId, ip, remarks, ex.getMessage());
    }
  }

  @Override
  public void logActivity(Action action, Long userId, String ip) {
    logActivity(action, userId, ip, "");
  }

}
