package com.telemetry.engine.auth.core.user.persistence.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.auth.core.user.persistence.UserPersistence;
import com.telemetry.engine.auth.core.user.respository.UserRepository;
import com.telemetry.engine.common.exception.DataPersistenceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserPersistenceImpl implements UserPersistence {

  @Autowired
  private UserRepository userRepository;

  @Override
  public User save(User user) {
    Assert.notNull(user, "User must not be null");
    try {
      return userRepository.save(user);
    } catch (DataAccessException ex) {
      log.error("DB error while saving user: email={}", user.getPrimaryEmail(), ex);
      throw new DataPersistenceException("Failed to save user");
    }
  }


  @Override
  public Optional<User> findByUsername(String username) {
    Assert.hasText(username, "Username must not be null or empty");
    try {
      return userRepository.findByUsername(username);
    } catch (DataAccessException ex) {
      log.error("DB error while fetching user: username={}", username, ex);
      throw new DataPersistenceException("Failed to fetch user");
    }
  }


  @Override
  public Optional<User> findByEmail(String primaryEmail) {
    Assert.hasText(primaryEmail, "Email must not be null or empty");
    try {
      return userRepository.findByPrimaryEmail(primaryEmail);
    } catch (DataAccessException ex) {
      log.error("DB error while fetching user: email={}", primaryEmail, ex);
      throw new DataPersistenceException("Failed to fetch user");
    }
  }


  @Override
  public Optional<User> findById(Long userId) {
    Assert.notNull(userId, "User ID must not be null or empty");
    try {
      return userRepository.findById(userId);
    } catch (DataAccessException ex) {
      log.error("DB error while fetching user: user ID={}", userId, ex);
      throw new DataPersistenceException("Failed to fetch user");
    }
  }


}
