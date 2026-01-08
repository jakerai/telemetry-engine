package com.telemetry.engine.auth.core.role.contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RoleAware {
  Set<String> getRoles();

  Set<String> getPermissions();

  void setRoles(Set<String> roles);

  void setPermissions(Set<String> permissions);

  @JsonIgnore
  default List<String> getRolesAsList() {
    Set<String> roles = getRoles();
    return roles == null ? Collections.emptyList() : new ArrayList<>(roles);
  }

  @JsonIgnore
  default List<String> getPermissionsAsList() {
    Set<String> permissions = getPermissions();
    return permissions == null ? Collections.emptyList() : new ArrayList<>(permissions);
  }

}
