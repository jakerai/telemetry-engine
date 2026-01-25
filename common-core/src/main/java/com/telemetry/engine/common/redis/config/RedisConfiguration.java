package com.telemetry.engine.common.redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.telemetry.engine.common.redis.RedisService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;

/**
 * Singleton Redis configuration for the Telemetry Engine.
 * Enforces a single client and connection lifecycle.
 */
public class RedisConfiguration {

  private static final Logger log = LoggerFactory.getLogger(RedisConfiguration.class);

  private final RedisClient redisClient;

  private final StatefulRedisConnection<String, String> connection;
  private final RedisReactiveCommands<String, String> reactiveCommands;

  private final StatefulRedisPubSubConnection<String, String> pubSubConnection;
  private final RedisPubSubReactiveCommands<String, String> pubSubReactive;

  private final RedisService redisService;

  private static volatile RedisConfiguration instance;

  // Private constructor to enforce singleton
  private RedisConfiguration(String redisUri) {
      log.info("Initializing RedisClient at {}", redisUri);
      this.redisClient = RedisClient.create(redisUri);

      // Command connection
      this.connection = redisClient.connect();
      this.reactiveCommands = connection.reactive();

      // Pub/Sub connection
      this.pubSubConnection = redisClient.connectPubSub();
      this.pubSubReactive = pubSubConnection.reactive();

      // Core RedisService wrapper
      this.redisService = new RedisService(redisClient, connection);

      // JVM shutdown hook to close gracefully
      Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
  }

  /**
   * Initialize the singleton RedisConfiguration with URI. Must call once at startup.
   */
  public static RedisConfiguration initialize(String redisUri) {
      if (instance == null) {
          synchronized (RedisConfiguration.class) {
              if (instance == null) {
                  instance = new RedisConfiguration(redisUri);
              }
          }
      }
      return instance;
  }

  /**
   * Get the initialized singleton instance.
   */
  public static RedisConfiguration getInstance() {
      if (instance == null) {
          throw new IllegalStateException("RedisConfiguration not initialized. Call initialize() first.");
      }
      return instance;
  }

  /**
   * Gracefully shutdown Redis connections.
   */
  public void shutdown() {
      try {
          log.info("Shutting down Redis connections...");
          if (connection != null) {
              connection.close();
          }
          if (pubSubConnection != null) {
              pubSubConnection.close();
          }
          if (redisClient != null) {
              redisClient.shutdown();
          }
          log.info("Redis shutdown complete.");
      } catch (Exception e) {
          log.error("Error during Redis shutdown", e);
      }
  }

  // --- Getters ---

  /** Core RedisService wrapper for common-core usage */
  public RedisService getRedisService() {
      return redisService;
  }

  /** Reactive command API for Mono/Flux operations */
  public RedisReactiveCommands<String, String> getReactiveCommands() {
      return reactiveCommands;
  }

  /** Direct Redis connection (blocking) */
  public StatefulRedisConnection<String, String> getConnection() {
      return connection;
  }

  /** Pub/Sub reactive commands */
  public RedisPubSubReactiveCommands<String, String> getPubSubReactive() {
      return pubSubReactive;
  }

  /** Pub/Sub blocking connection */
  public StatefulRedisPubSubConnection<String, String> getPubSubConnection() {
      return pubSubConnection;
  }

  /** Low-level Redis client */
  public RedisClient getClient() {
      return redisClient;
  }

}
