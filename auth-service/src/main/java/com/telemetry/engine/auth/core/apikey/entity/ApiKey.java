package com.telemetry.engine.auth.core.apikey.entity;

import java.time.Instant;
import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
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
@Table(name = "asset_keys", indexes = {@Index(name = "idx_key", columnList = "key", unique = true),
    @Index(name = "idx_asset_key_status", columnList = "revoked, expires_at")})
public class ApiKey extends BaseEntity {

  @Column(name = "key", nullable = false, unique = true)
  private String key;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Builder.Default
  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  @Column(name = "userId", nullable = false)
  private Long userId;

  @Builder.Default
  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Builder.Default
  @Column(name = "deleted", nullable = false)
  private boolean deleted = false;

  public boolean isValid() {
    return active && !revoked && (expiresAt == null || expiresAt.isAfter(Instant.now()));
  }

 
}
