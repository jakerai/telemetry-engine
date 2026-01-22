package com.telemetry.engine.common.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;

@Slf4j
public class JsonMapperUtil {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Converts a Java object to its JSON string representation.
   */
  public static String serializeToJson(Object object) {
      if (object == null) return null;
      try {
          return objectMapper.writeValueAsString(object);
      } catch (JacksonException e) {
          log.error("Serialization failed for class: {}", object.getClass().getName(), e);
          throw new RuntimeException("Error serializing object to JSON", e);
      }
  }

  /**
   * Converts a JSON string to a Java object of the specified class.
   */
  public static <T> T deserializeFromJson(String json, Class<T> targetClass) {
      if (json == null || json.isBlank()) return null;
      try {
          return objectMapper.readValue(json, targetClass);
      } catch (JacksonException e) {
          log.error("Deserialization failed for class: {}", targetClass.getSimpleName(), e);
          throw new IllegalArgumentException("Error deserializing JSON", e);
      }
  }
  
  public static <T> T deserializeFromJson(String json, TypeReference<T> typeReference) {
    try {
        return objectMapper.readValue(json, typeReference);
    } catch (JacksonException e) {
        log.error("JSON Deserialization failed", e);
        throw new RuntimeException("Failed to parse telemetry data", e);
    }
}

  /**
   * Converts a JSON array string to a List of Java objects.
   */
  public static <T> List<T> deserializeJsonToList(String json, Class<T> elementClass) {
      if (json == null || json.isBlank()) return List.of();
      CollectionType listType = objectMapper.getTypeFactory()
                                           .constructCollectionType(List.class, elementClass);
      try {
          return objectMapper.readValue(json, listType);
      } catch (JacksonException e) {
          log.error("Failed to convert JSON to List<{}>", elementClass.getSimpleName(), e);
          throw new IllegalArgumentException("Error deserializing JSON list", e);
      }
  }
  
  /**
   * Converts any DTO / object to Map<String, Object>
   * Useful for Redis hashes, dynamic updates, logging, etc.
   */
  public static Map<String, Object> toMap(Object object) {
      if (object == null) return Map.of();

      try {
          return objectMapper.convertValue(
                  object,
                  objectMapper.getTypeFactory()
                              .constructMapType(Map.class, String.class, Object.class)
          );
      } catch (IllegalArgumentException e) {
          log.error("Failed to convert object to Map: {}", object.getClass().getSimpleName(), e);
          throw new RuntimeException("Error converting object to Map", e);
      }
  }

  /**
   * Maps URL query parameters to a Java DTO.
   * Example: "lat=40.7&lon=-74.0" -> LocationRequest object
   */
  public static <T> T mapQueryParamsToDto(String query, Class<T> targetClass) {
      if (query == null || query.isBlank()) return instantiate(targetClass);

      try {
          Map<String, String> paramsMap = Arrays.stream(query.split("&"))
                  .map(p -> p.split("=", 2))
                  .filter(p -> p.length > 0)
                  .collect(Collectors.toMap(
                          p -> p[0],
                          p -> p.length > 1 ? p[1] : "",
                          (existing, replacement) -> existing
                  ));

          return objectMapper.convertValue(paramsMap, targetClass);
      } catch (Exception e) {
          log.error("Mapping failure for query string '{}' to class: {}", query, targetClass.getSimpleName(), e);
          throw new IllegalArgumentException("Invalid query parameters", e);
      }
  }

  /**
   * Instantiates a class using its default constructor.
   */
  private static <T> T instantiate(Class<T> clazz) {
      try {
          return clazz.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
          log.error("Failed to instantiate class: {}", clazz.getSimpleName(), e);
          return null;
      }
  }
  
}
