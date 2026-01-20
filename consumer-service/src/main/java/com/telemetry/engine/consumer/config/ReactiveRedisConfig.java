package com.telemetry.engine.consumer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.telemetry.engine.common.redis.RedisService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;

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
   * Creates a RedisClient. Put your Redis password in the URI if required.
   */
  @Bean(destroyMethod = "shutdown")
  public RedisClient redisClient() {
    return RedisClient.create(redisUri);
  }

  /**
   * Creates a reactive Redis connection for data operations.
   */
  @Bean(destroyMethod = "close")
  @Primary
  public StatefulRedisConnection<String, String> redisConnection(RedisClient client) {
    return client.connect();
  }

  /**
   * Creates a reactive Redis Pub/Sub connection.
   */
  @Bean(destroyMethod = "close")
  public StatefulRedisPubSubConnection<String, String> redisPubSubConnection(RedisClient client) {
    return client.connectPubSub();
  }

  /**
   * Creates a fully reactive RedisService from common-core. All operations (get/set, hash, sets,
   * publish/subscribe) return Mono / Flux.
   */
  @Bean
  public RedisService redisService(RedisClient redisClient,
      @Qualifier("redisConnection") StatefulRedisConnection<String, String> connection) {
    return new RedisService(redisClient, connection);
  }

  /**
   * Optional helper beans if you want to directly inject reactive commands. Can be used internally
   * in advanced scenarios.
   */
  @Bean
  public RedisReactiveCommands<String, String> reactiveCommands(
      StatefulRedisConnection<String, String> connection) {
    return connection.reactive();
  }

  @Bean
  public RedisPubSubReactiveCommands<String, String> reactivePubSubCommands(
      StatefulRedisPubSubConnection<String, String> pubSubConnection) {
    return pubSubConnection.reactive();
  }

}
