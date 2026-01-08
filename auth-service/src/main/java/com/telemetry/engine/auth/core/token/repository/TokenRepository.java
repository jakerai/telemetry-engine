package com.telemetry.engine.auth.core.token.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.token.entity.Token;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {

  Optional<Token> findByValue(String value);

}
