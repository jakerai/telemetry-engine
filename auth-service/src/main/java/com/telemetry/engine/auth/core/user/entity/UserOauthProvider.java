package com.telemetry.engine.auth.core.user.entity;

import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "user_oauth_providers", schema = "auth",
    uniqueConstraints = {@UniqueConstraint(name = "uk_provider_provider_user_id",
        columnNames = {"provider", "provider_user_id"})},
    indexes = {@Index(name = "idx_user_oauth_user_id", columnList = "user_id"),
        @Index(name = "idx_user_oauth_provider", columnList = "provider"),
        @Index(name = "idx_user_oauth_provider_user_id", columnList = "provider_user_id")})
public class UserOauthProvider extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "provider", length = 50, nullable = false)
  private String provider; // google, github, facebook

  @Column(name = "provider_user_id", length = 100, nullable = false)
  private String providerUserId;
}
