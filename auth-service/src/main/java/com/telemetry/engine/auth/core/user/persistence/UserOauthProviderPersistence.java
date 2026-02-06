package com.telemetry.engine.auth.core.user.persistence;

import java.util.Optional;
import com.telemetry.engine.auth.core.user.entity.UserOauthProvider;

public interface UserOauthProviderPersistence {

  UserOauthProvider save(UserOauthProvider userOauthProvider);
  
  Optional<UserOauthProvider> findByUserIdAndProvider(Long userId, String provider);
}
