package com.telemetry.engine.auth.security.jwt.key.enums;


/**
 * Represents the status of a JWT signing key.
 *
 * <p>
 * CURRENT - The active key used for signing new JWTs. PREVIOUS - The previously active key,
 * retained to verify existing JWTs issued before key rotation.
 * </p>
 */
public enum JwtKeyStatus {
  CURRENT, PREVIOUS
}
