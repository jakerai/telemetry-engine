package com.telemetry.engine.auth.security.jwt.key.model;

import java.time.Instant;
import com.telemetry.engine.auth.security.jwt.key.enums.JwtKeyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Key {
  private String kid;
  private String publicKey;
  private String encryptedPrivateKey;
  private JwtKeyStatus status;
  private Instant expiresAt;
}
