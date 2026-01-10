package com.telemetry.engine.ingestion.kafka.serializer;

import org.apache.kafka.common.serialization.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer<Object> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public byte[] serialize(String topic, Object data) {
    try {
      if (data == null) {
        return null;
      }
      return objectMapper.writeValueAsBytes(data);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing object to JSON", e);
    }
  }

}
