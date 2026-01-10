package com.telemetry.engine.auth.util;

import java.util.Arrays;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.telemetry.engine.auth.security.model.AuthenticatedUser;
import com.telemetry.engine.common.exception.UnauthorizedException;

public final class UserAccessValidator {
  private static final Logger log = LoggerFactory.getLogger(UserAccessValidator.class);


  private UserAccessValidator() {}

  /**
   * Checks if the current user has at least one of the required roles. Throws UnauthorizedException
   * if not allowed.
   */
  public static void validateUserAccess(String... allowedRoles) {
    AuthenticatedUser authenticatedUser = AuthUtil.getCurrentUserOrThrow();
    log.debug(
        "Validating roles for user ID={}: allowedRoles={}, userRoles={}",
        authenticatedUser.getId(), Arrays.toString(allowedRoles),
        authenticatedUser.getAuthorities());

    Set<String> allowedRoleSet = Set.copyOf(Arrays.asList(allowedRoles));
    boolean hasRole = authenticatedUser.getRoles().stream().anyMatch(allowedRoleSet::contains);

    if (!hasRole) {
      log.warn("Role validation failed for user ID={}", authenticatedUser.getId());
      throw new UnauthorizedException("You do not have permission to perform this operation");
    }
    log.debug("Role validation passed for user ID={}", authenticatedUser.getId());
  }


}
