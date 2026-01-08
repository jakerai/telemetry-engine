package com.telemetry.engine.auth.core.verification.service.impl;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.verification.entity.Verification;
import com.telemetry.engine.auth.core.verification.enums.CodeSendStatus;
import com.telemetry.engine.auth.core.verification.enums.VerificationChannel;
import com.telemetry.engine.auth.core.verification.enums.VerificationIntent;
import com.telemetry.engine.auth.core.verification.persistence.VerificationPersistence;
import com.telemetry.engine.auth.core.verification.service.VerificationService;
import com.telemetry.engine.auth.core.verification.util.VerificationCodeGenerator;
import com.telemetry.engine.auth.email.EmailService;
import com.telemetry.engine.common.exception.InvalidCodeException;
import com.telemetry.engine.common.exception.NotFoundException;
import com.telemetry.engine.common.exception.TooManyAttemptsException;
import com.telemetry.engine.common.utils.SecretUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
  private static final Logger log = LoggerFactory.getLogger(VerificationServiceImpl.class);

  private static final int MAX_ATTEMPTS = 3;

  private final VerificationPersistence verificationPersistence;
  private final EmailService emailService;


  @Override
  public void sendVerification(UserDto userDto, VerificationIntent intent,
      VerificationChannel channel) {
    log.info("[VerificationServiceImpl.sendVerification] Sending for {} verification", channel);

    String target = resolveTarget(userDto, intent, channel);

    String code = VerificationCodeGenerator.generateCode(channel);

    long expiresInMins = VerificationCodeGenerator.expiryFor(channel);

    Verification verification =
        createVerificationRecord(userDto.getId(), code, channel, intent, target, expiresInMins);

    if (target == null || target.isBlank()) {
      log.debug("Verification skipped: intent={}, channel={}, userId={}", intent, channel,
          userDto.getId());
      return;
    }

    log.info("Sending verification: intent={}, channel={}, target={}", intent, channel, target);
    try {
      switch (channel) {
        case EMAIL -> sendEmailVerification(code, intent, target, userDto.getFirstName());
        case MOBILE_NUMBER -> sendMobileNumberVerification(code, target);
        default -> throw new IllegalStateException("Unsupported channel: " + channel);
      }

      verification.setSendStatus(CodeSendStatus.SENT);
      verificationPersistence.save(verification);

    } catch (Exception ex) {
      log.error("Error while sending verification: intent={}, channel={}, target={}", intent,
          channel, target, ex);

      verification.setSendStatus(CodeSendStatus.FAILED);
      verification.setSendAttemptCount(verification.getSendAttemptCount() + 1);

      verificationPersistence.save(verification);
    }

  }


  @Override
  public void verifyCode(Long userId, String code) {
    log.info("[VerificationServiceImpl.verifyCode] Verifying code for user ID={}", userId);
    String codeHash = SecretUtils.hash(code);

    Verification verification =
        verificationPersistence.findByUserIdAndCode(userId, codeHash).orElseThrow(() -> {
          log.warn("Verification code not found: userId={}", userId);
          return new NotFoundException("Verification code invalid");
        });

    // Attempts exceeded
    if (verification.getVerificationAttemptCount() >= MAX_ATTEMPTS) {
      throw new TooManyAttemptsException("Maximum verification attempts exceeded");
    }

    // Already used
    if (verification.getVerifiedAt() != null) {
      incrementAttempts(verification);
      throw new InvalidCodeException("Verification code already used");
    }

    // Expired
    if (verification.getExpiresAt().isBefore(Instant.now())) {
      incrementAttempts(verification);
      throw new InvalidCodeException("Verification code expired");
    }


    // Marking verified
    verification.setVerifiedAt(Instant.now());
    verificationPersistence.save(verification);
  }

  private void incrementAttempts(Verification verification) {
    log.info(
        "[VerificationServiceImpl.incrementAttempts] Incrementing verification attempts count for user ID={}",
        verification.getUserId());

    verification.setVerificationAttemptCount(verification.getVerificationAttemptCount() + 1);
    verificationPersistence.save(verification);
  }

  private Verification createVerificationRecord(Long userId, String code,
      VerificationChannel channel, VerificationIntent intent, String target, Long expiresInMins) {
    log.info(
        "[VerificationServiceImpl.createVerificationRecord] Creating {} verification record for user ID={}",
        intent, userId);

    Verification verification = Verification.builder().userId(userId).channel(channel)
        .intent(intent).target(target).code(SecretUtils.hash(code))
        .expiresAt(Instant.now().plus(Duration.ofMinutes(expiresInMins))).build();

    return verificationPersistence.save(verification);
  }


  private void sendMobileNumberVerification(String code, String mobileNumber) {
    log.info(
        "[VerificationServiceImpl.sendMobileNumberVerification] Sending verification code to mobile number={}",
        mobileNumber);
    // verificationService.create(user.getId(), VerificationChannel.PHONE,
    // VerificationIntent.SIGNUP, user.getPhone(), otp, Duration.ofMinutes(10));

    // smsSenderService.sendOtp(user.getPhone(), otp);
  }

  private void sendPasswordResetEmail(String code, String receiverVerifiedEmail,
      String receiverFirstName) {
    log.info(
        "[VerificationServiceImpl.sendPasswordResetEmail] Sending password reset email to email={}",
        receiverVerifiedEmail);

    String verificationLink = "http://mydomain/verify?code=" + code;
    String body = "<p>Hi " + receiverFirstName + ",</p>" + "<p>Please verify your email:</p>"
        + "<a href=\"" + verificationLink + "\">Password Reset</a>";

    emailService.sendEmail(receiverVerifiedEmail, "Reset Your Password", body);
  }

  private void sendSignupEmail(String code, String receiverEmail, String receiverFirstName) {
    log.info("[VerificationServiceImpl.sendSignupEmail] Sending signup verification email to ={}",
        receiverEmail);

    String verificationLink = "http://mydomain/verify?code=" + code;
    String body = "<p>Hi " + receiverFirstName + ",</p>" + "<p>Please verify your email:</p>"
        + "<a href=\"" + verificationLink + "\">Verify Email</a>";

    emailService.sendEmail(receiverEmail, "Verify Your Email", body);
  }

  private void sendChangeEmailVerification(String code, String receiverNewEmail,
      String receiverFirstName) {
    log.info(
        "[VerificationServiceImpl.sendChangeEmailVerification] Sending email change verification to ={}",
        receiverNewEmail);

    String verificationLink = "http://mydomain/verify?code=" + code;
    String body = "<p>Hi " + receiverFirstName + ",</p>" + "<p>Please verify your email:</p>"
        + "<a href=\"" + verificationLink + "\">Verify New Email</a>";

    emailService.sendEmail(receiverNewEmail, "Verify Your New Email", body);

  }

  private String resolveTarget(UserDto userDto, VerificationIntent intent,
      VerificationChannel channel) {

    log.info(
        "[VerificationServiceImpl.resolveTarget] Resolving target for {} verification for userId={}",
        intent, userDto.getId());

    return switch (intent) {

      case SIGNUP -> channel == VerificationChannel.EMAIL ? userDto.getEmail()
          : userDto.getMobileNumber();

      case PASSWORD_RESET -> userDto.getEmail();

      case CHANGE_EMAIL -> userDto.getNewEmail();

      case CHANGE_MOBILE_NUMBER -> userDto.getNewMobileNumber();

      default -> null;
    };
  }

  private void sendEmailVerification(String code, VerificationIntent intent, String email,
      String firstName) {
    log.info(
        "[VerificationServiceImpl.sendEmailVerification] Sending email for {} verification for email={}",
        intent, email);

    switch (intent) {
      case SIGNUP -> sendSignupEmail(code, email, firstName);
      case PASSWORD_RESET -> sendPasswordResetEmail(code, email, firstName);
      case CHANGE_EMAIL -> sendChangeEmailVerification(code, email, firstName);
      default -> throw new IllegalStateException(
          "Email verification not supported for intent: " + intent);
    }
  }

}
