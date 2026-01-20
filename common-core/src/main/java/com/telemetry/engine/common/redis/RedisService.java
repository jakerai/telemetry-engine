package com.telemetry.engine.common.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.telemetry.engine.common.mapper.MapperUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Lightweight Redis service built on Lettuce.
 * 
 * Supports KV, Sets, Hashes, and Pub/Sub - JSON serialization via MapperUtil
 */
public class RedisService {

  private final RedisReactiveCommands<String, String> commands;
  private final RedisClient redisClient;

  public RedisService(RedisClient redisClient, StatefulRedisConnection<String, String> connection) {
    this.redisClient = redisClient;
    this.commands = connection.reactive();
  }

  // ---------------------- Key/Value Operations ----------------------

  public Mono<Boolean> putValue(String key, Object value, Duration ttl) {
    return Mono.fromCallable(() -> MapperUtil.serializeToJson(value))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(json -> commands.psetex(key, ttl.toMillis(), json)).map("OK"::equals)
        .onErrorMap(e -> new IllegalStateException("Failed to put Redis key: " + key, e));
  }

  public <T> Mono<T> getValue(String key, Class<T> clazz) {
    return commands.get(key)
        .flatMap(json -> Mono.fromCallable(() -> MapperUtil.deserializeFromJson(json, clazz))
            .subscribeOn(Schedulers.boundedElastic()))
        .onErrorMap(e -> new IllegalStateException("Failed to get Redis key: " + key, e));
  }

  // ---------------------- Hash Operations ----------------------

  public <T> Mono<Long> putHash(String key, Map<String, T> fields) {
    return Mono
        .fromCallable(() -> fields.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> MapperUtil.serializeToJson(e.getValue()))))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(serializedMap -> commands.hset(key, serializedMap))
        .onErrorMap(e -> new IllegalStateException("Failed to put Redis hash: " + key, e));
  }

  /**
   * Retrieves a Redis hash and deserializes values into the specified type
   */
  /**
   * Retrieves a Redis hash and deserializes values into the specified type
   */
  public <T> Mono<Map<String, T>> getHash(String key, Class<T> clazz) {
    return commands.hgetall(key)
        .flatMap(kv -> Mono
            .fromCallable(
                () -> Map.entry(kv.getKey(), MapperUtil.deserializeFromJson(kv.getValue(), clazz)))
            .subscribeOn(Schedulers.boundedElastic()))
        .collectMap(Map.Entry::getKey, Map.Entry::getValue);
  }

  /**
   * Returns the union of multiple Redis sets as a Flux of members
   */
  public Flux<String> unionSets(List<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return Flux.empty();
    }

    // sunion returns Flux<String> - each member is emitted individually
    return commands.sunion(keys.toArray(new String[0]))
        .onErrorMap(e -> new IllegalStateException("Failed to union Redis sets: " + keys, e));
  }


  // ---------------------- Set Operations ----------------------

  public Mono<Boolean> addToSet(String key, String value) {
    return commands.sadd(key, value).map(count -> count > 0)
        .onErrorMap(e -> new IllegalStateException("Failed to add to Redis set: " + key, e));
  }

  public Mono<Boolean> removeFromSet(String key, String value) {
    return commands.srem(key, value).map(count -> count > 0)
        .onErrorMap(e -> new IllegalStateException("Failed to remove from Redis set: " + key, e));
  }

  public Flux<String> getSetMembers(String key) {
    return commands.smembers(key).onErrorMap(
        e -> new IllegalStateException("Failed to get members of Redis set: " + key, e));
  }

  // ---------------------- Pub/Sub Operations ----------------------

  /**
   * Returns a Flux that emits messages from a Redis channel.
   * Each subscriber gets its own dedicated Pub/Sub connection.
   */
  public Flux<String> subscribe(String channel) {
      return Flux.usingWhen(
              Mono.fromCallable(redisClient::connectPubSub), // acquire a new Pub/Sub connection
              connection -> Flux.create(sink -> {
                  RedisPubSubAdapter<String, String> listener = new RedisPubSubAdapter<>() {
                      @Override
                      public void message(String ch, String message) {
                          if (channel.equals(ch)) {
                              sink.next(message);
                          }
                      }
                  };

                  connection.addListener(listener);
                  connection.async().subscribe(channel);

                  // Cleanup when subscriber cancels
                  sink.onDispose(() -> {
                      connection.async().unsubscribe(channel);
                      connection.removeListener(listener);
                  });
              }, FluxSink.OverflowStrategy.BUFFER),
              connection -> Mono.fromCompletionStage(connection.closeAsync()) // release connection
      );
  }
  
  public Mono<Long> publish(String channel, Object message) {
    return Mono.fromCallable(() -> MapperUtil.serializeToJson(message))
        .subscribeOn(Schedulers.boundedElastic()).flatMap(json -> commands.publish(channel, json))
        .onErrorMap(e -> new IllegalStateException(
            "Failed to publish Redis message to channel: " + channel, e));
  }

}
