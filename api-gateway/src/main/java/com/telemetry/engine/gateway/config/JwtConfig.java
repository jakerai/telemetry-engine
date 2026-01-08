package com.telemetry.engine.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class JwtConfig {

  private final String baseUrl;

  public JwtConfig(@Value("${app.security.auth.service.base-url}") String baseUrl) {
    this.baseUrl = baseUrl;
  }


  /**
   * Reactive JWT decoder
   */
  @Bean
  public ReactiveJwtDecoder reactiveJwtDecoder() {
    log.info("[JwtConfig.reactiveJwtDecoder] Fetchig jwt decoder from base url={}", baseUrl);
    return NimbusReactiveJwtDecoder.withJwkSetUri(baseUrl + "/internal/v1/.well-known/jwks.json")
        .build();
  }

}
