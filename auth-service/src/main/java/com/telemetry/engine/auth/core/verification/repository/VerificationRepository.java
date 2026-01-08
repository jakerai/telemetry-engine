package com.telemetry.engine.auth.core.verification.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.telemetry.engine.auth.core.verification.entity.Verification;


@Repository
public interface VerificationRepository extends CrudRepository<Verification, String> {

  Optional<Verification> findByUserIdAndCode(Long userId, String tokenHash);

}
