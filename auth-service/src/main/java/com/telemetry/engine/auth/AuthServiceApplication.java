package com.telemetry.engine.auth;

import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.telemetry.engine.auth.security.model.AuthenticatedUser;
import com.telemetry.engine.auth.util.AuthUtil;
import com.telemetry.engine.common.exception.UnauthorizedException;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuthServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }

  /**
   * Provides the current auditor (user ID) for MongoDB auditing. Uses
   * AuthUtil.getCurrentUserOptional() to safely return an empty value when no user is
   * authenticated, avoiding exceptions during startup or background tasks, This ensures clean logs
   * and stable initialization without losing strict authentication where needed.
   * <p>
   * This is used to automatically fill the @CreatedBy and @LastModifiedBy fields in entities. It
   * fetches the ID of the currently authenticated user from the security context using
   * {@link com.telemetry.engine.auth.util.rental.app.security.auth.util.AuthUtil#getCurrentUserOptional()}.
   * </p>
   * 
   * @return The current user ID, which will be automatically assigned to @CreatedBy/@LastModifiedBy
   *         fields, or empty if the user is unauthenticated.
   */
  @Bean
  public AuditorAware<Long> auditorProvider() {
    return () -> {
      try {
        return AuthUtil.getCurrentUserOptional().map(AuthenticatedUser::getId);
      } catch (UnauthorizedException ex) {
        return Optional.empty();
      }
    };
  }


}
