package com.telemetry.engine.consumer.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;


@Configuration
public class KafkaConsumerConfig {

  @Value("${kafka.bootstrap.servers}")
  private String bootstrapServers; // Kafka broker addresses, comma-separated

  @Value("${kafka.username:}")
  private String username; // SASL username, optional

  @Value("${kafka.password:}")
  private String password; // SASL password, optional

  @Value("${kafka.security.protocol:PLAINTEXT}")
  private String securityProtocol; // PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL

  @Value("${kafka.security.sasl.mechanism:PLAIN}")
  private String saslMechanism; // SASL mechanism (PLAIN, SCRAM-SHA-256, etc.)

  @Value("${kafka.group.id:consumer-group}")
  private String groupId; // Consumer group ID for offset management
  
  @Value("${kafka.topic}")
  private String topic;

  @Bean
  public ReceiverOptions<String, String> receiverOptions() {
    Map<String, Object> props = new HashMap<>();
    
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // "latest" is also an option
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); 
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000); // Max time between polls
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Small batches for processing

    props.put("security.protocol", securityProtocol);
    if (securityProtocol.startsWith("SASL") && username != null && !username.isBlank()) {
      props.put("sasl.mechanism", saslMechanism);
      props.put("sasl.jaas.config", String.format(
          "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
          username, password));
    }

    return ReceiverOptions.<String, String>create(props);
  }


  @Bean
  public KafkaReceiver<String, String> kafkaReceiver(ReceiverOptions<String, String> options) {
    return KafkaReceiver.create(options.subscription(Collections.singleton(topic)));
  }

}
