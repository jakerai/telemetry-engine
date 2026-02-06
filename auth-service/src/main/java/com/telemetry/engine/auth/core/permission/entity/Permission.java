package com.telemetry.engine.auth.core.permission.entity;

import java.util.HashSet;
import java.util.Set;
import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import com.telemetry.engine.auth.core.role.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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
@Table(name = "permissions", schema = "auth",
    indexes = {@Index(name = "idx_permission", columnList = "type")})
public class Permission extends BaseEntity {

  @Column(name = "type", nullable = false, unique = true, length = 50)
  private String type;

  @Builder.Default
  @ManyToMany(mappedBy = "permissions", fetch = FetchType.EAGER)
  private Set<Role> roles = new HashSet<>();

}
