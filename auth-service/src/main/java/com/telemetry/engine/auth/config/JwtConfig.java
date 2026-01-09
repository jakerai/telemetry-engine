package com.telemetry.engine.auth.config;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.SignedJWT;
import com.telemetry.engine.auth.security.jwt.key.service.JwtKeyManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class JwtConfig {

  private final JwtKeyManager jwtKeyManager;

  public JwtConfig(JwtKeyManager jwtKeyManager) {
    this.jwtKeyManager = jwtKeyManager;
  }

  /**
   * The methods dynamically retrieves the current public and private keys and constructs an RSAKey
   * instance.
   * 
   * @return RSAKey
   */
  public RSAKey getCurrentRsaKey() {
    return new RSAKey.Builder(jwtKeyManager.getCurrentPublicKey())
        .privateKey(jwtKeyManager.getCurrentPrivateKey())
        .keyID(jwtKeyManager.getCurrentKey().getKid()).build();
  }


  /**
   * JwtEncoder uses the current key for signing new tokens
   */
  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(getCurrentRsaKey())));
  }

  /**
   * JwtDecoder dynamically fetches the correct public key based on token's 'kid' This ensures old
   * tokens signed with previous key still validate correctly until they are expired
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    return token -> {
      try {
        String tokenKid = SignedJWT.parse(token).getHeader().getKeyID();
        RSAPublicKey publicKey = jwtKeyManager.getPublicKeyByKid(tokenKid);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        decoder.setJwtValidator(tokenValidator());
        return decoder.decode(token);
      } catch (ParseException e) {
        throw new IllegalStateException("Failed to parse JWT", e);
      }
    };
  }

  /**
   * Custom token validator that allows refresh tokens to access only refresh-token REST APIs.
   */
  @Bean
  public OAuth2TokenValidator<Jwt> tokenValidator() {
    return token -> {
      if (token.getExpiresAt().isBefore(Instant.now())) {
        return OAuth2TokenValidatorResult
            .failure(new OAuth2Error("invalid_token", "Token expired", null));
      }
      String type = token.getClaimAsString("type");
      HttpServletRequest request =
          ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
      String path = request.getServletPath();

      if ("REFRESH".equals(type) && !path.equals("/auth/refresh-token")) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token",
            "Refresh token not allowed to access secured API", null));
      }
      return OAuth2TokenValidatorResult.success();
    };
  }
  
}
