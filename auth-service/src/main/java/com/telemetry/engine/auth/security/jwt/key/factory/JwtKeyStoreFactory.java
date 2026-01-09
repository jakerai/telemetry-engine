package com.telemetry.engine.auth.security.jwt.key.factory;

import org.springframework.stereotype.Component;
import com.telemetry.engine.auth.config.JwtProperties;
import com.telemetry.engine.auth.security.jwt.key.store.JwtKeyStore;
import com.telemetry.engine.auth.security.jwt.key.store.impl.AwsJwtKeyStore;
import com.telemetry.engine.auth.security.jwt.key.store.impl.DatabaseJwtKeyStore;
import lombok.RequiredArgsConstructor;

/**
 * Factory class that decides which JwtKeyStore implementation to use based on the configured key
 * source.
 *
 * <p>
 * This allows the application to switch between different key storage mechanisms (for example AWS
 * Secrets Manager or a database) without changing any business logic.
 * </p>
 *
 * @throws IllegalStateException if an unsupported key source is configured
 */
@Component
@RequiredArgsConstructor
public class JwtKeyStoreFactory {

  private final DatabaseJwtKeyStore databaseJwtKeyStore;
  private final AwsJwtKeyStore awsJwtKeyStore;
  private final JwtProperties jwtProperties;

  public JwtKeyStore getStore() {
    return switch (jwtProperties.getKeySource().toLowerCase()) {
      case "aws", "aws-secret" -> awsJwtKeyStore;
      case "db", "database" -> databaseJwtKeyStore;
      default -> throw new IllegalStateException(
          "Unsupported jwt.key-source: " + jwtProperties.getKeySource());
    };
  }

}
