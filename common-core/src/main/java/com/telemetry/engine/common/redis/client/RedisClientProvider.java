package com.telemetry.engine.common.redis.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

/**
 * Provides a single async Redis connection for high throughput.
 */
public class RedisClientProvider {

  private final RedisClient client;
  private final StatefulRedisConnection<String, String> connection;

  public RedisClientProvider(String redisUrl) {
      this.client = RedisClient.create(redisUrl);
      this.connection = client.connect();
  }

  public RedisAsyncCommands<String, String> async() {
      return connection.async();
  }

  public void shutdown() {
      connection.close();
      client.shutdown();
  }
  
}
