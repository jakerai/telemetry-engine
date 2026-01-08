package com.telemetry.engine.gateway.security.apikey.service;

import java.util.Set;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyMeta;
import reactor.core.publisher.Mono;

public interface ApiKeyAuthService {

  Mono<ApiKeyMeta> isValid(String apiKey);

  void syncKeys(Set<String> activeKeys);

}
