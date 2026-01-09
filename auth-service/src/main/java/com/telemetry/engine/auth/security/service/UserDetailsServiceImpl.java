package com.telemetry.engine.auth.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.auth.core.user.persistence.UserPersistence;
import com.telemetry.engine.auth.security.model.AuthenticatedUser;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

  @Autowired
  private UserPersistence userPersistence;

  @Cacheable(value = "usersCache", key = "#username")
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("[UserDetailsServiceImpl.loadUserByUsername] loading user by username={}", username);

    User user = userPersistence.findByUsername(username).orElseThrow(() -> {
      log.warn("User not found: username={}", username);
      return new UsernameNotFoundException("User not found");
    });
    // Checking user status before returning
    validateUserStatus(user);

    return new AuthenticatedUser(user);
  }

  private void validateUserStatus(User user) {
    log.info("[UserDetailsServiceImpl.validateUserStatus] Validating user status: email={}",
        user.getEmail());
    switch (user.getStatus()) {
      case BANNED:
        throw new DisabledException("Account is banned");
      case INACTIVE:
        throw new DisabledException("Account is inactive");
      case LOCKED:
        throw new DisabledException("Account is locked. Please contact support.");
      case SUSPENDED:
        throw new DisabledException("Account is suspended");
      case DELETED:
        throw new DisabledException("Account does not exist");
      // case PENDING_VERIFICATION:
      // throw new DisabledException("Account pending email verification");
      case ACTIVE:
      default:
        // User is active, continue
    }
    log.info("User status validation passed: email={}", user.getEmail());
  }

}
