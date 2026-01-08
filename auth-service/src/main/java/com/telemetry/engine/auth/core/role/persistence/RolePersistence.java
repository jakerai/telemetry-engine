package com.telemetry.engine.auth.core.role.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.telemetry.engine.auth.core.role.entity.Role;

public interface RolePersistence {

  Set<Role> findByIds(Set<Long> ids);

  Set<Role> findByNames(Set<String> names);

  List<Role> saveAll(List<Role> roles);

  Set<Role> findAll();

  Optional<Role> findByName(String name);

  Role save(Role role);

}
