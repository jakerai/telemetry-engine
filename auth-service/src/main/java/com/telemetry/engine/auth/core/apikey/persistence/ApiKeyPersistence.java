package com.telemetry.engine.auth.core.apikey.persistence;

import java.util.Optional;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;

public interface ApiKeyPersistence {
  
  ApiKey save(ApiKey apiKey);

  Optional<ApiKey> findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(String key);

  Optional<ApiKey> findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(Long id);
  
   
}
