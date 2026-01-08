package com.telemetry.engine.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class SecretUtils {
  private SecretUtils() {}

  /**
   * This method generates a cryptographic hash of the given token. This method can be used to
   * securely store or compare tokens without exposing the original value.
   *
   * @param token the input string to hashs
   * @return a hashed representation of the input token as a String.
   * 
   */
  public static String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Hashing algorithm not available", e);
    }
  }

  /**
   * This method masks a sensitive string according to its length.
   * <p>
   * Masking rules:
   * <ul>
   * <li>Length &lt; 6: mask all characters.</li>
   * <li>Length = 6: show first 2 characters, mask the remaining 4.</li>
   * <li>Length 7–8: show first 2 and last 2 characters, mask the middle characters.</li>
   * <li>Length ≥ 9: show first 2 and last 3 characters, mask the middle characters.</li>
   * </ul>
   * <p>
   * Examples:
   * 
   * <pre>
   * maskSensitiveData("123")           -> "###"
   * maskSensitiveData("123456")        -> "12####"
   * maskSensitiveData("1234567")       -> "12###67"
   * maskSensitiveData("12345678")      -> "12####78"
   * maskSensitiveData("123456789")     -> "12####789"
   * </pre>
   *
   * @param value the sensitive string to mask, may be null or empty
   * @return the masked string; returns null or empty string as-is
   */
  public static String maskSensitiveData(String value) {
    if (value == null || value.isEmpty())
      return value;

    int length = value.length();

    if (length < 6) {
      return "#".repeat(length);
    } else if (length == 6) {
      return value.substring(0, 2) + "#".repeat(length - 2);
    } else if (length <= 8) {
      // showing first 2 and last 2, masking middle
      return value.substring(0, 2) + "#".repeat(length - 4) + value.substring(length - 2);
    } else {
      // show first 2 and last 3, masking middle
      return value.substring(0, 2) + "#".repeat(length - 5) + value.substring(length - 3);
    }
  }



}
