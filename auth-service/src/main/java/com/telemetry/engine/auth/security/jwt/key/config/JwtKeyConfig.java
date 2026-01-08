package com.telemetry.engine.auth.security.jwt.key.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.telemetry.engine.auth.security.jwt.key.factory.JwtKeyStoreFactory;
import com.telemetry.engine.auth.security.jwt.key.store.JwtKeyStore;

@Configuration
public class JwtKeyConfig {

  @Bean
  public JwtKeyStore jwtKeyStore(JwtKeyStoreFactory factory) {
    return factory.getStore();
  }

}
