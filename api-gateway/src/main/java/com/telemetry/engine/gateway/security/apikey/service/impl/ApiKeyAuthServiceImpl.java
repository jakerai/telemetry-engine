package com.telemetry.engine.gateway.security.apikey.service.impl;

import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.telemetry.engine.common.utils.SecretUtils;
import com.telemetry.engine.gateway.security.apikey.client.AuthServiceClient;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyMeta;
import com.telemetry.engine.gateway.security.apikey.service.ApiKeyAuthService;
import com.telemetry.engine.gateway.security.cache.AuthCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyAuthServiceImpl implements ApiKeyAuthService {

  private final AuthCache authCache;
  private final AuthServiceClient authServiceClient;

  @Override
  public Mono<ApiKeyMeta> isValid(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      log.warn("[ApiKeyAuthService] Received blank API key");
      return Mono.empty();
    }

    // Hash the API key before lookup
    String apiKeyHash = SecretUtils.hash(apiKey);

    return authCache.get(apiKeyHash).flatMap(meta -> {
      
      if (meta.getExpiresAt() != null && Instant.now().isAfter(meta.getExpiresAt())) {
        // Key expired, invalidate reactively
        log.info("[ApiKeyAuthServiceImpl.isValid] API key {} expired at {}",
            SecretUtils.maskSensitiveData(apiKeyHash), meta.getExpiresAt());
        return authCache.invalidate(apiKeyHash).then(Mono.<ApiKeyMeta>empty());
      }

      log.debug("API key {} valid for userId={} assetId={}",
          SecretUtils.maskSensitiveData(apiKeyHash), meta.getUserId(), meta.getAssetId());
      return Mono.just(meta);
    }).switchIfEmpty(Mono.defer(() -> {
      // Fetching from AuthService if not in cache
      log.debug("API key {} not in cache so fetching from auth service",
          SecretUtils.maskSensitiveData(apiKeyHash));

      // Assuming fetchApiKeyMeta now returns Mono<ApiKeyMeta>
      return authServiceClient.fetchApiKeyMeta(apiKey).flatMap(fetchedMeta -> {
        if (fetchedMeta == null) {
          log.info("AuthService did not return any data as the key is invalid");
          return Mono.empty();
        }
        // Save in cache before returning
        log.info("AuthService returned data={}. Saving the data in cache",
             fetchedMeta);
        return authCache.put(apiKeyHash, fetchedMeta).thenReturn(fetchedMeta);
      });
    }));
  }


  @Override
  public void syncKeys(Set<String> activeKeys) {
    // TODO Auto-generated method stub

  }


}
