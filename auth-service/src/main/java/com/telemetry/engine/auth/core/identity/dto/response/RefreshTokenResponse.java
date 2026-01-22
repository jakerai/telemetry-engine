package com.telemetry.engine.auth.core.identity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {

  private String accessToken;
  private long accessTokenExpiresIn;
  private String refreshToken;
  private String tokenType;

  public static RefreshTokenResponse from(String accessToken, long accessTokenExpiresIn,
      String refreshToken) {
    return RefreshTokenResponse.builder().accessToken(accessToken)
        .accessTokenExpiresIn(accessTokenExpiresIn).refreshToken(refreshToken).tokenType("Bearer")
        .build();
  }

}
