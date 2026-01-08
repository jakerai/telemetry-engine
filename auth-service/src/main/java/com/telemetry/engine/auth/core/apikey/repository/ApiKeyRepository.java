package com.telemetry.engine.auth.core.apikey.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;

@Repository
public interface ApiKeyRepository extends CrudRepository<ApiKey, Long> {

  Optional<ApiKey> findByKeyAndActiveTrueAndRevokedFalseAndDeletedFalse(String key);

  Optional<ApiKey> findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(Long id);

}
