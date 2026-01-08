package com.telemetry.engine.auth.security.jwt.key.store;

import java.util.Optional;
import com.telemetry.engine.auth.security.jwt.key.model.JwtKeys;

public interface JwtKeyStore {

  Optional<JwtKeys> findKeys();

  void saveKeys(JwtKeys keys);

  /**
   * Rotate keys only if the current key has not changed.
   * Returns true if rotation succeeded, false otherwise.
   */
  boolean rotateKeysAtomically(String expectedCurrentKid, JwtKeys newKeys);
}
