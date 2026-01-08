package com.telemetry.engine.gateway.security.cache;

import java.util.Set;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyMeta;
import reactor.core.publisher.Mono;

public interface AuthCache {
  
  Mono<Boolean> isValid(String key);

  Mono<ApiKeyMeta> get(String key);

  Mono<Void> put(String key, ApiKeyMeta meta);

  Mono<Void> invalidate(String key);

  Mono<Void> syncKeys(Set<ApiKeyMeta> key);

}
