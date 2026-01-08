package com.telemetry.engine.auth.core.activity.service;

import com.telemetry.engine.auth.core.activity.enums.Action;

public interface ActivityService {

  void logActivity(Action action, Long userId, String ip, String remarks);

  void logActivity(Action action, Long userId, String ip);

}
