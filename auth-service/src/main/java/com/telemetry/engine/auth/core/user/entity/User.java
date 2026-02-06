package com.telemetry.engine.auth.core.user.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import com.telemetry.engine.auth.core.role.entity.Role;
import com.telemetry.engine.auth.core.user.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "users", schema = "auth",
    indexes = {@Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_primary_email", columnList = "primary_email"),
        @Index(name = "idx_user_created_at", columnList = "created_at")})
public class User extends BaseEntity {

  @Column(name = "username", nullable = false, unique = true, length = 255)
  private String username;

  @Column(name = "primary_email", nullable = false, unique = true, length = 320)
  private String primaryEmail;

  @Column(name = "primary_mobile_number", length = 20, nullable = true)
  private String primaryMobileNumber;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "password_login_enabled")
  @Builder.Default
  private boolean passwordLoginEnabled = false;

  @Column(name = "first_name", length = 100, nullable = true)
  private String firstName;

  @Column(name = "last_name", length = 100, nullable = true)
  private String lastName;

  @Column(name = "picture", length = 500, nullable = true)
  private String picture;

  @Column(name = "primary_email_verified")
  @Builder.Default
  private boolean primaryEmailVerified = false;

  @Column(name = "primary_mobile_number_verified")
  @Builder.Default
  private boolean primaryMobileNumberVerified = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  @Builder.Default
  private UserStatus status = UserStatus.PENDING_VERIFICATION;

  @Column(name = "failed_login_attempts", nullable = false)
  @Builder.Default
  private int failedLoginAttempts = 0;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @Column(name = "last_login_ip", length = 45)
  private String lastLoginIp;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Builder.Default
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

}
