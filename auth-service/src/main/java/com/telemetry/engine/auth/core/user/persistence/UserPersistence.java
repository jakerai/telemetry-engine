package com.telemetry.engine.auth.core.user.persistence;

import java.util.Optional;
import com.telemetry.engine.auth.core.user.entity.User;

public interface UserPersistence {

  User save(User user);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findById(Long userId);

}
