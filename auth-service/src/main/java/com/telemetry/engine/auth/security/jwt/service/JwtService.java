package com.telemetry.engine.auth.security.jwt.service;

import java.util.List;
import java.util.Map;
import com.telemetry.engine.auth.security.model.JwtToken;

public interface JwtService {

  JwtToken generateAccessTokenOrThrow(String subject, Long userId, List<String> roles,
      List<String> permissions, Long refreshTokenId);

  JwtToken generateRefreshTokenOrThrow(String subject, Long userId);

  boolean validateRefreshToken(String token);

  Map<String, Object> getJwks();

}
