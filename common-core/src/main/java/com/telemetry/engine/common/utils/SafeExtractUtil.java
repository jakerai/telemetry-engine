package com.telemetry.engine.common.utils;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Utility class that provides safe extraction helpers for reading values from external sources such
 * as JWT claims, request bodies, or any structure that may throw an exception during value
 * retrieval.
 *
 * <p>
 * This class helps avoid repetitive try/catch blocks and ensures that a default value is always
 * returned when something goes wrong — for example: missing claims, incorrect data types, null
 * values, or unexpected exceptions.
 * </p>
 *
 * <p>
 * <strong>Important:</strong> This class contains only pure utility methods. It is stateless,
 * thread-safe, and cannot be instantiated.
 * </p>
 * 
 * @author Vishal Rai
 * @version 1.0
 * @since 2025
 * @see om.rental.app.user.mapper.UserMapper
 */
public class SafeExtractUtil {

  private SafeExtractUtil() {} // prevent instantiation

  /**
   * Safely executes the given getter operation and returns the extracted value. If the getter
   * throws any exception (NullPointerException, ClassCastException, ParseException, etc.), the
   * provided default value is returned instead.
   *
   * @param getter the operation that extracts the desired value
   * @param defaultVal value to return if extraction fails
   * @param <T> the type of the value being extracted
   * @return extracted value, or defaultVal if an error occurs
   */
  public static <T> T safeGet(Callable<T> getter, T defaultVal) {
    try {
      return getter.call();
    } catch (Exception e) {
      return defaultVal;
    }
  }

  /**
   * Safely extracts a list of strings from a dynamic object, typically coming from JWT claim
   * structures or frameworks that return raw `Object` values.
   *
   * <p>
   * If the provided object is not a List, or contains mixed/non-string types, this method converts
   * all elements to strings using `toString()`.
   * </p>
   *
   * <p>
   * If extraction fails or the object is not a list, an empty list is returned.
   * </p>
   *
   * @param rolesObj raw object expected to contain a List of roles
   * @return list of role strings (never null)
   */
 
  public static List<String> safeGetList(Object rolesObj) {
    if (rolesObj instanceof List<?> list) {
      return list.stream().map(Object::toString).toList();
    }
    return List.of();
  }

}
