package com.telemetry.engine.ingestion.producer;

import java.time.Duration;
import java.util.List;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.telemetry.engine.common.mapper.JsonMapperUtil;
import com.telemetry.engine.ingestion.dto.MessageEvent;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionProducer {

  @Value("${kafka.topic}")
  private String topic;

  // Tunable batch size for high throughput: 1k per record
  private static final int BATCH_SIZE = 1000;

  // Max parallel batches to send concurrently
  private static final int MAX_PARALLEL_BATCHES = 4;

  private final KafkaSender<String, String> kafkaSender; // JSON string messages


  /**
   * Sends events in batches to Kafka. Each Kafka record contains a JSON array of events.
   */
  public Mono<Void> send(String key, List<MessageEvent> events, CircuitBreaker cb) {
    log.info("Ingesting {} events", events.size());

    return Flux.fromIterable(events).bufferTimeout(BATCH_SIZE, Duration.ofMillis(200))
        .onBackpressureBuffer(10_000,
            dropped -> log.warn("Dropped {} events due to backpressure", dropped.size()))
        .flatMap(batch -> sendBatchAsJsonArray(batch, key, cb), MAX_PARALLEL_BATCHES).then();
  }


  /**
   * Sends a single batch as one Kafka record (JSON array string)
   */
  private Mono<Void> sendBatchAsJsonArray(List<MessageEvent> batch, String key, CircuitBreaker cb) {
    try {
      // Converting entire batch to JSON array string
      String batchJson = JsonMapperUtil.serializeToJson(batch);

      // Creating Kafka record
      ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, batchJson);
      SenderRecord<String, String, List<MessageEvent>> senderRecord =
          SenderRecord.create(record, batch);

      // Sending the batch
      return kafkaSender.send(Mono.just(senderRecord))
          .transformDeferred(CircuitBreakerOperator.of(cb)).collectList().flatMap(results -> {
            long failed = results.stream().filter(r -> r.exception() != null).count();
            if (failed > 0) {
              log.error("Batch delivery failed: {}/{}", failed, results.size());
              return Mono.error(new RuntimeException("Kafka batch delivery failure"));
            }
            log.info("Batch sent successfully: {} events", batch.size());
            return Mono.empty();
          });

    } catch (Exception e) {
      log.error("Failed to serialize batch to JSON", e);
      return Mono.error(e);
    }
  }

}
