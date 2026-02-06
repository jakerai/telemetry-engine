package com.telemetry.engine.auth.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

  private Jwt jwt = new Jwt();
  private OAuth2 oauth2 = new OAuth2();

  @Data
  public static class Jwt {
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
  }

  /**
   * Number of days a key is valid
   */
  @Data
  public static class KeyRotation {
    private int validitySeconds;

    /**
     * Rotate key if it will expire in x days
     */
    private int rotateBeforeSeconds;
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



  @Data
  public static class OAuth2 {
    /**
     * Allowed redirect URIs for OAuth2 login
     */
    private List<String> allowedRedirectUris = new ArrayList<>();
  }


}
