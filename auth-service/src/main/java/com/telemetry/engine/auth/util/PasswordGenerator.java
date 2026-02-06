package com.telemetry.engine.auth.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGenerator {
  private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL = "$#@&*%!";
  private static final int LENGTH = 32;

  private static final SecureRandom random = new SecureRandom();

  public static String generate() {

    List<Character> passwordChars = new ArrayList<>();

    /* Mandatory characters */
    passwordChars.add(randomChar(UPPER));
    passwordChars.add(randomChar(DIGITS));
    passwordChars.add(randomChar(SPECIAL));

    /* Remaining characters */
    String allAllowed = LOWER + UPPER + DIGITS + SPECIAL;
    for (int i = passwordChars.size(); i < LENGTH; i++) {
      passwordChars.add(randomChar(allAllowed));
    }

    /* Shuffling to avoid predictable positions */
    Collections.shuffle(passwordChars, random);

    /* Converting to String */
    StringBuilder password = new StringBuilder();
    for (char c : passwordChars) {
      password.append(c);
    }

    return password.toString();
  }

  private static char randomChar(String chars) {
    return chars.charAt(random.nextInt(chars.length()));
  }
}
