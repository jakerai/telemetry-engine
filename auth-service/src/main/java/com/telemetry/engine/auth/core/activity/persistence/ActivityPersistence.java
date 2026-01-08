package com.telemetry.engine.auth.core.activity.persistence;

import com.telemetry.engine.auth.core.activity.entity.Activity;

public interface ActivityPersistence {

  Activity save(Activity activityLog);
  
}
