package com.telemetry.engine.auth.core.role.persistence.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;
import com.telemetry.engine.auth.core.role.entity.Role;
import com.telemetry.engine.auth.core.role.persistence.RolePersistence;
import com.telemetry.engine.auth.core.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RolePersistenceImpl implements RolePersistence {

  private final RoleRepository roleRepository;

  @Override
  public Set<Role> findByIds(Set<Long> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) {
      return Collections.emptySet();
    }
    return Optional.ofNullable(roleRepository.findByIdIn(roleIds)).orElse(Collections.emptySet());
  }

  @Override
  public Set<Role> findByNames(Set<String> roleNames) {
    if (roleNames == null || roleNames.isEmpty()) {
      return Collections.emptySet();
    }
    return Optional.ofNullable(roleRepository.findByNameIn(roleNames))
        .orElse(Collections.emptySet());
  }

  @Override
  public List<Role> saveAll(List<Role> roles) {
    Iterable<Role> saved = roleRepository.saveAll(roles);
    return StreamSupport.stream(saved.spliterator(), false).collect(Collectors.toList());
  }

  @Override
  public Set<Role> findAll() {
    return roleRepository.findAll();
  }

  @Override
  public Optional<Role> findByName(String name) {
    return roleRepository.findByName(name);
  }

  @Override
  public Role save(Role role) {
    return roleRepository.save(role);
  }

}
