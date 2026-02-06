package com.telemetry.engine.auth.core.role.entity;

import java.util.HashSet;
import java.util.Set;
import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import com.telemetry.engine.auth.core.permission.entity.Permission;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "role", schema = "auth",
    indexes = {@Index(name = "idx_role_name", columnList = "name")})
public class Role extends BaseEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Builder.Default
  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"}))
  private Set<Permission> permissions = new HashSet<>();

  public void addPermission(Permission permission) {
    permissions.add(permission);
    permission.getRoles().add(this);
  }

  public void removePermission(Permission permission) {
    permissions.remove(permission);
    permission.getRoles().remove(this);
  }

  public boolean hasPermission(Permission permission) {
    return permissions.contains(permission);
  }

}
