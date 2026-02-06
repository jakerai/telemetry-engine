package com.telemetry.engine.auth.core.user.respository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.user.entity.UserOauthProvider;

@Repository
public interface UserOauthProviderRepository extends CrudRepository<UserOauthProvider, Long> {

  Optional<UserOauthProvider> findByUser_IdAndProvider(Long userId, String provider);
}
