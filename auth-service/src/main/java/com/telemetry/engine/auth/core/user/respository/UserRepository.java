package com.telemetry.engine.auth.core.user.respository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.user.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

  Optional<User> findByPrimaryEmail(String primaryEmail);

  Optional<User> findByUsername(String username);

}
