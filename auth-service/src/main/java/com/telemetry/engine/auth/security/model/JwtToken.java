package com.telemetry.engine.auth.security.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class JwtToken {
  private String value;
  private String username;
  private String userId;
  private List<String> roles;
  private String type;
  private String refreshTokenId;
  private Instant expirationTime;
  private Long expiresIn;
}
