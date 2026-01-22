package com.telemetry.engine.auth.core.identity.dto.response;

import com.telemetry.engine.auth.core.user.dto.UserDto;
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
public class LoginResponse {
  private String accessToken;
  private long accessTokenExpiresIn;
  private String refreshToken;
  private String tokenType;
  private UserDto user;

  public static LoginResponse from(String accessToken, long accessTokenExpiresIn,
      String refreshToken, UserDto user) {
    return LoginResponse.builder().accessToken(accessToken)
        .accessTokenExpiresIn(accessTokenExpiresIn).refreshToken(refreshToken).tokenType("Bearer")
        .user(user).build();
  }
}
