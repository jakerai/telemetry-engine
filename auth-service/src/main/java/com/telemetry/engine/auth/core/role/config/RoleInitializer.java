package com.telemetry.engine.auth.core.role.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.telemetry.engine.auth.core.permission.entity.Permission;
import com.telemetry.engine.auth.core.permission.persistence.PermissionPersistence;
import com.telemetry.engine.auth.core.role.entity.Role;
import com.telemetry.engine.auth.core.role.persistence.RolePersistence;
import com.telemetry.engine.common.constansts.SystemConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer {

  private final RolePersistence rolePersistence;
  private final PermissionPersistence permissionPersistence;
  private final RoleConfig roleConfig;

  private static Set<String> ALL_ROLES = Collections.emptySet();


  public static Set<String> getAllRoles() {
    return ALL_ROLES;
  }

  @PostConstruct
  public void loadRolesAndPermissions() {
    log.info("[RoleInitializer.loadRolesAndPermissions] Loading roles and permissions");
    if (roleConfig.getRoles() == null || roleConfig.getRoles().isEmpty()) {
      log.warn("No roles defined in YAML. Skipping RBAC initialization.");
      return;
    }


    // Loading existing roles
    Map<String, Role> roleMap =
        rolePersistence.findAll().stream().collect(Collectors.toMap(Role::getName, r -> r));

    // Inserting missing roles
    roleConfig.getRoles().forEach(yamlRole -> {
      roleMap.computeIfAbsent(yamlRole.getName(), roleName -> {
        Role role = new Role();
        role.setName(roleName);
        role.setCreatedBy(SystemConstants.SYSTEM_ID);
        return rolePersistence.save(role);
      });
    });

    // Loading permissions
    Map<String, Permission> permissionMap = permissionPersistence.findAll().stream()
        .collect(Collectors.toMap(Permission::getType, p -> p));

    // Insert missing permissions for that role
    roleConfig.getRoles().forEach(yamlRole -> {
      yamlRole.getPermissions().forEach(permissionName -> {
        permissionMap.computeIfAbsent(permissionName, type -> {
          Permission permission = new Permission();
          permission.setType(type);
          permission.setCreatedBy(SystemConstants.SYSTEM_ID);
          return permissionPersistence.save(permission);
        });
      });
    });

    // Assigning permissions to roles
    roleConfig.getRoles().forEach(yamlRole -> {
      Role role = roleMap.get(yamlRole.getName());

      Set<Permission> desiredPermissions =
          yamlRole.getPermissions().stream().map(permissionMap::get).collect(Collectors.toSet());

      if (!new HashSet<>(role.getPermissions()).equals(desiredPermissions)) {
        role.getPermissions().clear();
        desiredPermissions.forEach(role::addPermission);
        rolePersistence.save(role);
      }
    });

    // Populating cache
    ALL_ROLES = rolePersistence.findAll().stream().map(Role::getName)
        .collect(Collectors.toUnmodifiableSet());

    log.info("RBAC initialization completed. Loaded number of roles={}", ALL_ROLES.size());
  }

}
