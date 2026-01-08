package com.telemetry.engine.gateway.security.cache.impl;

import java.time.Instant;
import java.util.Set;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyMeta;
import com.telemetry.engine.gateway.security.cache.AuthCache;
import reactor.core.publisher.Mono;

@Component
public class RedisAuthCache implements AuthCache {

  private final ReactiveRedisTemplate<String, ApiKeyMeta> redisTemplate;

  public RedisAuthCache(ReactiveRedisTemplate<String, ApiKeyMeta> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Mono<Boolean> isValid(String apiKey) {
    return redisTemplate.opsForValue().get(apiKey).flatMap(meta -> {
      if (meta == null) {
        return Mono.just(false);
      }

      if (meta.getExpiresAt() == null) {
        /**
         * If an API key has no expiry date, treat it as non-expiring (permanent key)
         */
        return Mono.just(true);
      }

      boolean valid = meta.getExpiresAt().isAfter(Instant.now());

      if (!valid) {
        // cleaning up expired key
        return redisTemplate.opsForValue().delete(apiKey).thenReturn(false);
      }

      return Mono.just(true);
    }).defaultIfEmpty(false); // key not found in Redis
  }

  @Override
  public Mono<ApiKeyMeta> get(String apiKey) {
    return redisTemplate.opsForValue().get(apiKey)
        .flatMap(meta -> meta.getExpiresAt().isAfter(Instant.now()) ? Mono.just(meta)
            : redisTemplate.opsForValue().delete(apiKey).then(Mono.empty()));
  }

  @Override
  public Mono<Void> put(String apiKey, ApiKeyMeta meta) {
    return redisTemplate.opsForValue().set(apiKey, meta).then();
  }

  @Override
  public Mono<Void> invalidate(String apiKey) {
    return redisTemplate.opsForValue().delete(apiKey).then();
  }

  @Override
  public Mono<Void> syncKeys(Set<ApiKeyMeta> keys) {
    // Clearing Redis keys and pushing new keys
    return redisTemplate.keys("*").flatMap(redisTemplate.opsForValue()::delete)
        .then(Mono.when(keys.stream().map(k -> put(k.getApiKeyHash(), k)).toArray(Mono[]::new)));
  }

}
