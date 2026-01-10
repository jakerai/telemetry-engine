package com.telemetry.engine.auth.core.token.persistence.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.telemetry.engine.auth.core.token.entity.Token;
import com.telemetry.engine.auth.core.token.persistence.TokenPersistence;
import com.telemetry.engine.auth.core.token.repository.TokenRepository;
import com.telemetry.engine.common.exception.DataPersistenceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenPersistenceImpl implements TokenPersistence {

  @Autowired
  private TokenRepository tokenRepository;

  @Override
  public Token save(Token token) {
    Assert.notNull(token, "Token must not be null");
    try {
      return tokenRepository.save(token);
    } catch (DataAccessException ex) {
      log.error("DB error while saving token: user ID={}",
          token.getUserId(), ex);
      throw new DataPersistenceException("Failed to save token");
    }
  }


  @Override
  public Optional<Token> findById(Long tokenId) {
    Assert.notNull(tokenId, "Token ID must not be null or empty");
    try {
      return tokenRepository.findById(tokenId);
    } catch (DataAccessException ex) {
      log.error("DB error while fetching token: tokenId={}",
          tokenId, ex);
      throw new DataPersistenceException("Failed to fetch token");
    }
  }


  @Override
  public Optional<Token> findByValue(String value) {
    Assert.hasText(value, "Token value must not be null or empty");
    try {

      return tokenRepository.findByValue(value);
    } catch (DataAccessException ex) {
      log.error("DB error while fetching token: value={}", value,
          ex);
      throw new DataPersistenceException("Failed to fetch token");
    }
  }


}
