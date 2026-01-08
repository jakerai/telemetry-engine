package com.telemetry.engine.auth.core.token.service.impl;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.token.dto.TokenDto;
import com.telemetry.engine.auth.core.token.entity.Token;
import com.telemetry.engine.auth.core.token.mapper.TokenMapper;
import com.telemetry.engine.auth.core.token.persistence.TokenPersistence;
import com.telemetry.engine.auth.core.token.service.TokenService;
import com.telemetry.engine.auth.security.jwt.enums.TokenType;
import com.telemetry.engine.common.exception.DataPersistenceException;
import com.telemetry.engine.common.exception.InvalidTokenException;
import com.telemetry.engine.common.exception.NotFoundException;
import com.telemetry.engine.common.utils.SecretUtils;

@Service
public class TokenServiceImpl implements TokenService {
  private static final Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

  @Autowired
  private TokenPersistence tokenPersistence;

  @Override
  public TokenDto storeTokenOrThrow(Long userId, String value, TokenType tokenType,
      Instant expiresAt) {
    String hashedToken = SecretUtils.hash(value);
    log.info("[TokenServiceImpl.storeTokenOrThrow] Saving token for userId={}", userId);

    Token token = Token.builder().userId(userId).value(hashedToken).expiresAt(expiresAt)
        .type(tokenType).build();
    try {
      token = tokenPersistence.save(token);
      return TokenMapper.toTokenDto(token);
    } catch (Exception ex) {
      log.error("[TokenServiceImpl.storeTokenOrThrow] Error while saving token for userId={}", userId, ex);
      throw new DataPersistenceException("Failed to save Token");
    }
  }

  @Override
  public void revokeTokenById(Long tokenId) {
    log.info("[TokenServiceImpl.revokeTokenById] Revoking token with tokenId={}", tokenId);

    Token token = tokenPersistence.findById(tokenId).orElse(null);

    if (token == null || Boolean.TRUE.equals(token.isRevoked())) {
      log.info("Token missing or already revoked: tokenId={}", tokenId);
      return;
    }

    token.setRevoked(true);
    token.setExpiresAt(Instant.now());

    try {
      tokenPersistence.save(token);
      log.info("Token successfully revoked: tokenId={}", tokenId);
    } catch (Exception ex) {
      log.error("Error while revoking token by tokenId={}", tokenId, ex);
      throw new DataPersistenceException("Failed to revoke token");
    }
  }


  @Override
  public void revokeTokenByValue(String value) {
    String hashedToken = SecretUtils.hash(value);
    log.info("[TokenServiceImpl.revokeTokenByValue] Revoking token with value={}", hashedToken);
    
    Token token = tokenPersistence.findByValue(hashedToken).orElse(null);

    if (token == null || Boolean.TRUE.equals(token.isRevoked())) {
      log.info("Token missing or already revoked: value={}", hashedToken);
      return;
    }

    token.setRevoked(true);
    token.setExpiresAt(Instant.now());

    try {
      tokenPersistence.save(token);
      log.info("Token successfully revoked using value: tokenId={}", token.getId());
    } catch (Exception ex) {
      log.error("Error while revoking token by value={}", hashedToken, ex);
      throw new DataPersistenceException("Failed to revoke token");
    }
  }


  @Override
  public void validateTokenOrThrow(String value) {
    String hashedToken = SecretUtils.hash(value);
    log.info("[TokenServiceImpl.validateTokenOrThrow] validating token with value={}", hashedToken);
    Token token = tokenPersistence.findByValue(hashedToken)
        .orElseThrow(() -> {
            log.debug("Token not found={}", hashedToken);
            return new NotFoundException("Token is invalid");
        });

    if (Boolean.TRUE.equals(token.isRevoked())) {
      log.debug("Token is already revoked");
      throw new InvalidTokenException("Refresh token is no longer valid");
    }

    if (token.getExpiresAt().isBefore(Instant.now())) {
      log.debug("Token already expired");
      throw new InvalidTokenException("Token expired");
    }
  }

}
