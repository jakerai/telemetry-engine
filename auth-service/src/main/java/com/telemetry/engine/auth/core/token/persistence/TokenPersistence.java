package com.telemetry.engine.auth.core.token.persistence;

import java.util.Optional;
import com.telemetry.engine.auth.core.token.entity.Token;

public interface TokenPersistence {

  Token save(Token token);

  Optional<Token> findById(Long tokenId);

  Optional<Token> findByValue(String value);

}
