package com.telemetry.engine.auth.core.apikey.persistence.impl;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;
import com.telemetry.engine.auth.core.apikey.persistence.ApiKeyPersistence;
import com.telemetry.engine.auth.core.apikey.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ApiKeyPersistenceImpl implements ApiKeyPersistence {

  private final ApiKeyRepository apiKeyRepository;

  @Override
  public ApiKey save(ApiKey apiKey) {

    return apiKeyRepository.save(apiKey);
  }

  @Override
  public Optional<ApiKey> findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(String key) {
    return apiKeyRepository.findByKeyAndActiveTrueAndRevokedFalseAndDeletedFalse(key);
  }

  @Override
  public Optional<ApiKey> findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(Long id) {
    return apiKeyRepository.findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(id);
  }


}
