package com.telemetry.engine.auth.core.verification.entity;

import java.time.Instant;
import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import com.telemetry.engine.auth.core.verification.enums.CodeSendStatus;
import com.telemetry.engine.auth.core.verification.enums.VerificationChannel;
import com.telemetry.engine.auth.core.verification.enums.VerificationIntent;
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
@Table(name = "verification",
    indexes = {@Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_created_at", columnList = "created_at")})
public class Verification extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel", nullable = false, length = 50)
  private VerificationChannel channel;

  @Column(name = "code", nullable = false, length = 150)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(name = "intent", nullable = false, length = 50)
  private VerificationIntent intent;

  @Column(name = "target", nullable = false, length = 320)
  private String target; // email or mobile number

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "verified_at")
  private Instant verifiedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "send_status", nullable = false, length = 50)
  @Builder.Default
  private CodeSendStatus sendStatus = CodeSendStatus.PENDING;

  @Column(name = "send_attempt_count")
  @Builder.Default
  private int sendAttemptCount = 0;

  @Column(name = "verification_attempt_count")
  @Builder.Default
  private int verificationAttemptCount = 0;

  public boolean isVerified() {
    return verifiedAt != null;
  }

  public boolean isExpired() {
    return expiresAt.isBefore(Instant.now());
  }
}
