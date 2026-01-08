package com.telemetry.engine.gateway.security.cache.impl;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyMeta;
import com.telemetry.engine.gateway.security.cache.AuthCache;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "app.security.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineAuthCache implements AuthCache {

  private final Cache<String, ApiKeyMeta> cache;

  public CaffeineAuthCache() {
    this.cache =
        Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maximumSize(100_000).build();
  }

  @Override
  public Mono<Boolean> isValid(String apiKey) {
    return Mono.fromSupplier(() -> {
      ApiKeyMeta meta = cache.getIfPresent(apiKey);

      if (meta == null) {
        return false;
      }

      if (meta.getExpiresAt() == null) {
        /**
         * If an API key has no expiry date, treat it as non-expiring (permanent key)
         */
        return true;
      } 

      boolean valid = meta.getExpiresAt().isAfter(java.time.Instant.now());

      if (!valid) {
        // cleaning up expired key
        cache.invalidate(apiKey);
      }

      return valid;
    });
  }

  @Override
  public Mono<ApiKeyMeta> get(String apiKey) {
    ApiKeyMeta meta = cache.getIfPresent(apiKey);
    if (meta != null && meta.getExpiresAt().isBefore(Instant.now())) {
      cache.invalidate(apiKey);
      return Mono.empty();
    }
    return Mono.justOrEmpty(meta);
  }

  @Override
  public Mono<Void> put(String apiKey, ApiKeyMeta meta) {
    cache.put(apiKey, meta);
    return Mono.empty();
  }

  @Override
  public Mono<Void> invalidate(String apiKey) {
    cache.invalidate(apiKey);
    return Mono.empty();
  }

  @Override
  public Mono<Void> syncKeys(Set<ApiKeyMeta> keys) {
    cache.invalidateAll();
    keys.forEach(k -> cache.put(k.getApiKeyHash(), k));
    return Mono.empty();
  }


}
