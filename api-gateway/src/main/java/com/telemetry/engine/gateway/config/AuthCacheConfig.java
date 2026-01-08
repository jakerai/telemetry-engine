package com.telemetry.engine.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.telemetry.engine.gateway.security.cache.AuthCache;
import com.telemetry.engine.gateway.security.cache.factory.AuthCacheFactory;

@Configuration
public class AuthCacheConfig {

  @Bean
  public AuthCache authCache(AuthCacheFactory factory) {
    return factory.getType();
  }

}
