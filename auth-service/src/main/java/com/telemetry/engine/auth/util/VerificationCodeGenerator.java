package com.telemetry.engine.auth.util;

import java.security.SecureRandom;
import com.telemetry.engine.auth.core.verification.enums.VerificationChannel;

public final class VerificationCodeGenerator {

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int CODE_LENGTH = 6;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private VerificationCodeGenerator() {}

  public static String generateCode(VerificationChannel channel) {
    return channel == VerificationChannel.EMAIL ? generateCode() : generateOtp();
  }

  public static long expiryFor(VerificationChannel channel) {
    // 2880 mins for email and 5 mins for mobile code
    return channel == VerificationChannel.EMAIL ? 2880 : 5;
  }


  // For Email
  public static String generateCode() {
    return SECURE_RANDOM.ints(CODE_LENGTH, 0, CHARACTERS.length()).mapToObj(CHARACTERS::charAt)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
  }

  // For SMS OTP (6 digits)
  private static String generateOtp() {
    return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
  }

}
