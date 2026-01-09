package com.telemetry.engine.gateway.security.cache.factory;

import org.springframework.stereotype.Component;
import com.telemetry.engine.gateway.config.SecurityProperties;
import com.telemetry.engine.gateway.security.cache.AuthCache;
import com.telemetry.engine.gateway.security.cache.impl.CaffeineAuthCache;
import com.telemetry.engine.gateway.security.cache.impl.RedisAuthCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCacheFactory {

  private final SecurityProperties securityProperties;
  private final CaffeineAuthCache caffeineAuthCache;
  private final RedisAuthCache redisAuthCache;


  public AuthCache getType() {
    log.info("Loading cache type={}", securityProperties.getType());
    return switch (securityProperties.getType().toLowerCase()) {
      case "caffe", "caffeine" -> caffeineAuthCache;
      case "redis", "elasticache" -> redisAuthCache;
      default -> throw new IllegalStateException(
          "Unsupported cache type: " + securityProperties.getType());
    };
  }

}
