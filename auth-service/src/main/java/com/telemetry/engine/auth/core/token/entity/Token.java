package com.telemetry.engine.auth.core.token.entity;

import java.time.Instant;
import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import com.telemetry.engine.auth.security.jwt.enums.TokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "token",
    indexes = {@Index(name = "idx_token_user_id", columnList = "user_id"),
        @Index(name = "idx_token_value", columnList = "value"),
        @Index(name = "idx_token_type", columnList = "type"),
        @Index(name = "idx_token_expires_at", columnList = "expires_at"),
        @Index(name = "idx_token_revoked", columnList = "revoked")})
public class Token extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  /**
   * Store HASHED token value
   */
  @Column(name = "value", nullable = false, length = 512)
  private String value;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private TokenType type;

  @Builder.Default
  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  public boolean isExpired() {
    return expiresAt.isBefore(Instant.now());
  }

  public boolean isActive() {
    return !revoked && !isExpired();
  }

}
