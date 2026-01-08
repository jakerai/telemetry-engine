package com.telemetry.engine.ingestion.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.telemetry.engine.ingestion.dto.IngestionRequest;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public Mono<Void> send(IngestionRequest request) {
    log.info("[IngestionProducer.send] Sending event");
    return Mono.fromFuture(kafkaTemplate.send("ingestion-topic", request))
        .doOnSuccess(result -> log.debug("[IngestionProducer.send] Sent event to partition {} successfully",
            result.getRecordMetadata().partition()))
        .doOnError(e -> log.error("[IngestionProducer.send] Failed to send event", e)).then();
  }

  @PreDestroy
  public void shutdown() {
    try {
      log.info("[IngestionProducer.shutdown] Flushing and closing Kafka producer...");

      // Flushing any pending messages
      kafkaTemplate.flush();

      // Destroying underlying producer safely
      kafkaTemplate.destroy();

      log.info("[IngestionProducer.shutdown] Kafka producer shutdown completed.");
    } catch (Exception e) {
      log.error("[IngestionProducer.shutdown] Error during Kafka shutdown", e);
    }
  }

}
