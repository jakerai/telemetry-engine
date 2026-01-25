package com.telemetry.engine.query.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.telemetry.engine.common.geo.H3Service;
import com.telemetry.engine.common.redis.RedisService;
import com.telemetry.engine.common.redis.config.RedisConfiguration;
import com.telemetry.engine.common.redis.reader.RedisAssetStateReader;

/**
 * Reactive Redis configuration using Lettuce.
 *
 * This configuration creates: RedisClient bean. Stateful connections for data and pub/sub.
 * RedisService bean which exposes reactive operations (Mono / Flux)
 *
 * Works with common-core RedisService without Spring Reactive RedisTemplate.
 */
@Configuration
public class ReactiveRedisConfig {

  @Value("${redis.uri}")
  private String redisUri;

  /**
   * Initializes Redis singleton and provides RedisAssetStateWriter bean.
   */
  @Bean
  public RedisAssetStateReader redisAssetStateReader() {
      
      RedisConfiguration.initialize(redisUri);
      RedisService redisService = RedisConfiguration.getInstance().getRedisService();

      // Create reader
      return new RedisAssetStateReader(redisService, h3Service());
  }

  @Bean
  public H3Service h3Service() {
    return new H3Service();
  }
}
