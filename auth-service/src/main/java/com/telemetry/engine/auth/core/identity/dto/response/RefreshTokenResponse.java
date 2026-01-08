package com.telemetry.engine.auth.core.identity.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long accessTokenExpiresIn;
}
