package com.telemetry.engine.auth.core.role.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.role.entity.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

  Set<Role> findByIdIn(Set<Long> roleIds);

  Set<Role> findByNameIn(Set<String> roleNames);

  Set<Role> findAll();

  Optional<Role> findByName(String name);

}
