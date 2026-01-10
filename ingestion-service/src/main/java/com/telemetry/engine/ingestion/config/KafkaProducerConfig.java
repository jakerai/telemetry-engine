package com.telemetry.engine.ingestion.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.telemetry.engine.ingestion.kafka.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaProducerConfig {

  @Value("${kafka.bootstrap.servers}")
  private String bootstrapServers;

  @Value("${kafka.username}")
  private String username;

  @Value("${kafka.password}")
  private String password;

  @Value("${kafka.security.protocol}") // PLAINTEXT or SASL_PLAINTEXT/SASL_SSL
  private String securityProtocol;

  @Value("${kafka.security.sasl.mechanism}")
  private String saslMechanism;

  @Bean
  public KafkaSender<String, Object> kafkaSender() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // Serializer for the message key (String -> bytes)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    // Serializer for the message value (Object -> JSON)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);


    // wait up to 5ms to batch messages
    props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
    // Batch size in bytes (per partition) 128KB
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 131072);
    // Compress batches to reduce network usage and improve throughput
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
    // Leader must acknowledge, faster than waiting for all replicas
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // no duplicates
    props.put(ProducerConfig.RETRIES_CONFIG, 3); // retry 3 times
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134217728); // 128MB buffer

    // Security settings if (SASL/SSL)
    if (!username.isEmpty() && !password.isEmpty() && !saslMechanism.isEmpty()) {
      props.put("security.protocol", securityProtocol);
      props.put("sasl.mechanism", saslMechanism);
      props.put("sasl.jaas.config",
          "org.apache.kafka.common.security.scram.ScramLoginModule required " + "username=\""
              + username + "\" password=\"" + password + "\";");
    }

    SenderOptions<String, Object> senderOptions = SenderOptions.create(props);
    return KafkaSender.create(senderOptions);
  }

}
