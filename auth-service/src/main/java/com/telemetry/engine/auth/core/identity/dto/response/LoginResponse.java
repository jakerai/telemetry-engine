package com.telemetry.engine.auth.core.identity.dto.response;

import com.telemetry.engine.auth.core.user.dto.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long accessTokenExpiresIn;
  private UserDto user;
}
