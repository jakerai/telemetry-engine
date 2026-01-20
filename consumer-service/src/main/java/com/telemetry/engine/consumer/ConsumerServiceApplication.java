package com.telemetry.engine.consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.telemetry.engine.consumer.kafka.KafkaConsumer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SpringBootApplication
public class ConsumerServiceApplication implements CommandLineRunner {

  private final KafkaConsumer kafkaConsumer;

  public static void main(String[] args) {
    SpringApplication.run(ConsumerServiceApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    kafkaConsumer.consume();
  }
  

}
