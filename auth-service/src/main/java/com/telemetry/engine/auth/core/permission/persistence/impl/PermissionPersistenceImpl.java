package com.telemetry.engine.auth.core.permission.persistence.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;
import com.telemetry.engine.auth.core.permission.entity.Permission;
import com.telemetry.engine.auth.core.permission.persistence.PermissionPersistence;
import com.telemetry.engine.auth.core.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermissionPersistenceImpl implements PermissionPersistence {

  private final PermissionRepository permissionRepository;

  @Override
  public List<Permission> findAll() {
    return StreamSupport.stream(permissionRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public Permission save(Permission permission) {
    return permissionRepository.save(permission);
  }


}
