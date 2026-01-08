package com.telemetry.engine.auth.core.identity.util;

import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import com.telemetry.engine.auth.security.AuthenticatedUser;
import com.telemetry.engine.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthUtil {

  private AuthUtil() {}

  /**
   * Get the of the currently authenticated user from the token.
   *
   * @return Returns the authenticated user most recent data, or throws UnauthorizedException if
   *         missing
   */
  public static AuthenticatedUser getCurrentUserOrThrow() {
    return getCurrentUserOptional().orElseThrow(() -> {
      log.error("User authentication failed");
      return new UnauthorizedException("No authenticated user found");
    });
  }

  /**
   * Returns Optional.empty() if user not authenticated, no exceptions
   * 
   */
  public static Optional<AuthenticatedUser> getCurrentUserOptional() {
    log.info("[AuthUtil.getCurrentUserOptional] Retrieving current user");

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      log.debug("No authenticated user found or user is anonymous");
      return Optional.empty();
    }

    Object principal = auth.getPrincipal();

    // If using AuthenticatedUser as principal
    if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
      log.debug("Principal is not an instance of AuthenticatedUser, found: {}",
          principal != null ? principal.getClass().getSimpleName() : "null");
      return Optional.empty();
    }

    Jwt jwt = authenticatedUser.getCurrentJwt();
    Long userId = jwt.getClaim("userId");
    if (userId == null || userId == 0) {
      log.debug("JWT claim userId is null or invalid: {}", userId);
      return Optional.empty();
    }

    return Optional.of(authenticatedUser);
  }


}
