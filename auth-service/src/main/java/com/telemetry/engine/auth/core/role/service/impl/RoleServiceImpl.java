package com.telemetry.engine.auth.core.role.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.role.entity.Role;
import com.telemetry.engine.auth.core.role.persistence.RolePersistence;
import com.telemetry.engine.auth.core.role.service.RoleService;
import com.telemetry.engine.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
  private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

  private final RolePersistence rolePersistence;

  @Override
  public Role getRoleByNameOrThrow(String name) {
    log.info("Fetching role by name={}", name);
    return rolePersistence.findByName(name).orElseThrow(() -> {
      log.warn("Role not found: name={}", name);
      return new NotFoundException("Role not found");
    });
  }

}
