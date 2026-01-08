package com.telemetry.engine.auth.core.permission.persistence;

import java.util.List;
import com.telemetry.engine.auth.core.permission.entity.Permission;

public interface PermissionPersistence {

  List<Permission> findAll();

  Permission save(Permission permission);

}
