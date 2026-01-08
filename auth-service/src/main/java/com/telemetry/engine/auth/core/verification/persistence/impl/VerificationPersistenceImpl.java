package com.telemetry.engine.auth.core.verification.persistence.impl;

import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import com.telemetry.engine.auth.core.verification.entity.Verification;
import com.telemetry.engine.auth.core.verification.persistence.VerificationPersistence;
import com.telemetry.engine.auth.core.verification.repository.VerificationRepository;
import com.telemetry.engine.common.exception.DataPersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationPersistenceImpl implements VerificationPersistence {

  private final VerificationRepository verificationRepository;

  @Override
  public Verification save(Verification verification) {
    Assert.notNull(verification, "Verification must not be null");
    try {
      return verificationRepository.save(verification);
    } catch (DataAccessException ex) {
      log.error(
          "[VerificationPersistenceImpl.save] DB error while saving verification code: user ID={}",
          verification.getUserId(), ex);
      throw new DataPersistenceException("Failed to save verification code");
    }
  }

  @Override
  public Optional<Verification> findByUserIdAndCode(Long userId, String codeHash) {
    Assert.notNull(userId, "User ID must not be null or empty");
    Assert.hasText(codeHash, "Code hash must not be null or empty");
    try {
      return verificationRepository.findByUserIdAndCode(userId, codeHash);
    } catch (DataAccessException ex) {
      log.error(
          "[VerificationPersistenceImpl.findByUserIdAndCode] DB error while fetching verfication code: user ID={}",
          userId, ex);
      throw new DataPersistenceException("Failed to fetch verification");
    }
  }


}
