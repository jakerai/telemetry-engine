package com.telemetry.engine.auth.core.role.service;

import com.telemetry.engine.auth.core.role.entity.Role;

public interface RoleService {

  Role getRoleByNameOrThrow(String name);
  
}
