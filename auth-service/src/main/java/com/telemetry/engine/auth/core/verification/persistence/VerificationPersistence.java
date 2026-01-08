package com.telemetry.engine.auth.core.verification.persistence;

import java.util.Optional;
import com.telemetry.engine.auth.core.verification.entity.Verification;

public interface VerificationPersistence {

  Verification save(Verification verification);

  Optional<Verification> findByUserIdAndCode(Long userId, String codeHash);

}
