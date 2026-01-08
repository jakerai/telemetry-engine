package com.telemetry.engine.auth.security.jwt.key.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
  private Long accessExpiry;
  private Long refreshExpiry;
  private String issuer;


  /**
   * Which source to use for loading keys at startup. Supported: database, aws secrets manager
   */
  private String keySource = "database";

  /**
   * Key rotation settings
   */
  private KeyRotation rotation = new KeyRotation();

  /**
   * RSA key settings
   */
  private Rsa rsa = new Rsa();

  /**
   * Encryption settings (used to encrypt private keys at rest)
   */
  private Encryption encryption = new Encryption();

  @Data
  public static class KeyRotation {
    /**
     * Number of days a key is valid
     */
    private int validitySeconds = 1;

    /**
     * Rotate key if it will expire in x days
     */
    private int rotateBeforeSeconds = 1;

  }

  @Data
  public static class Rsa {
    /**
     * Key size in bits (2048 recommended)
     */
    private int keySize = 2048;
  }

  @Data
  public static class Encryption {
    /**
     * Master secret used to encrypt private keys. Inject via environment variable or AWS Secrets
     * Manager
     */
    private String masterSecret;
  }

}
