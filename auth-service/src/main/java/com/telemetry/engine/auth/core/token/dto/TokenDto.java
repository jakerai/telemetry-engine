package com.telemetry.engine.auth.core.token.dto;

import java.time.Instant;
import com.telemetry.engine.auth.common.base.dto.BaseDto;
import com.telemetry.engine.auth.security.jwt.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TokenDto extends BaseDto {
  private Long userId;
  private TokenType type;
  private Boolean revoked;
  private Instant expiresAt;
}
