package com.telemetry.engine.auth.core.token.service;

import java.time.Instant;
import com.telemetry.engine.auth.core.token.dto.TokenDto;
import com.telemetry.engine.auth.security.jwt.enums.TokenType;

public interface TokenService {

  TokenDto storeTokenOrThrow(Long userId, String token, TokenType tokenType, Instant expiresAt);

  void revokeTokenById(Long tokenId);

  void revokeTokenByValue(String value);

  void validateTokenOrThrow(String tokenValue);

}
