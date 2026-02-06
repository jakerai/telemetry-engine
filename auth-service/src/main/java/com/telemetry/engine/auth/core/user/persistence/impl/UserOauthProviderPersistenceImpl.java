package com.telemetry.engine.auth.core.user.persistence.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.telemetry.engine.auth.core.user.entity.UserOauthProvider;
import com.telemetry.engine.auth.core.user.persistence.UserOauthProviderPersistence;
import com.telemetry.engine.auth.core.user.respository.UserOauthProviderRepository;
import com.telemetry.engine.common.exception.DataPersistenceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserOauthProviderPersistenceImpl implements UserOauthProviderPersistence {

  @Autowired
  private UserOauthProviderRepository userOauthProviderRepository;

  @Override
  public UserOauthProvider save(UserOauthProvider userOauthProvider) {
    Assert.notNull(userOauthProvider, "UserOauthProvider must not be null");
    try {
      return userOauthProviderRepository.save(userOauthProvider);
    } catch (DataAccessException ex) {
      log.error("DB error while saving UserOauthProvider: oauth provider ID={}",
          userOauthProvider.getProviderUserId(), ex);
      throw new DataPersistenceException("Failed to save UserOauthProvider");
    }
  }

  @Override
  public Optional<UserOauthProvider> findByUserIdAndProvider(Long userId, String provider) {
    try {
      return userOauthProviderRepository.findByUser_IdAndProvider(userId, provider);
    } catch (DataAccessException ex) {
      log.error("DB error while fetching UserOauthProvider: user ID={}, provider={}", userId,
          provider, ex);
      throw new DataPersistenceException("Failed to fetch UserOauthProvider");
    }
  }


}
