package com.telemetry.engine.ingestion.kafka.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    // Configuration map for Kafka producer
    Map<String, Object> config = new HashMap<>();

    // ================= BASIC =================
    // Kafka broker address
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    // Serializer for the message key (String -> bytes)
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    // Serializer for the message value (Object -> JSON)
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    // ================= PERFORMANCE =================
    // wait up to 5ms to batch messages
    config.put(ProducerConfig.LINGER_MS_CONFIG, 5);
    // Batch size in bytes (per partition) 128KB
    config.put(ProducerConfig.BATCH_SIZE_CONFIG, 131072);
    config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134217728); // 128MB buffer
    // Compress batches to reduce network usage and improve throughput
    config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
    // Wait up to 60s to complete sends before failing
    config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);

    // ================= RELIABILITY =================
    // Leader must acknowledge, faster than waiting for all replicas
    config.put(ProducerConfig.ACKS_CONFIG, "1");
    config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // no duplicates
    config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

    // ================= RETRY =================
    config.put(ProducerConfig.RETRIES_CONFIG, 3); // retry 3 times
    config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // wait 100ms between retries
    config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // 30s request timeout
    config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 2 min total retry window

    // Create the producer factory with the configuration
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

}
