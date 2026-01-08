package com.telemetry.engine.auth.core.permission.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.permission.entity.Permission;

@Repository
public interface PermissionRepository extends CrudRepository<Permission, Long> {

  Optional<Permission> findByType(String type);

}
