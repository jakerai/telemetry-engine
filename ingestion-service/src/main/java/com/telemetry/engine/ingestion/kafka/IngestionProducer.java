package com.telemetry.engine.ingestion.kafka;

import java.util.List;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import com.telemetry.engine.ingestion.dto.MessageRequest;
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

  private static final String TOPIC = "ingestion-topic";
  private static final int BATCH_SIZE = 1000;
  private static final int MAX_PARALLEL_BATCHES = 4; // adjusting based on Kafka cluster capacity

  private final KafkaSender<String, Object> kafkaSender;

  public Mono<Void> send(String key, List<MessageRequest> requests, CircuitBreaker cb) {
    log.info("Processing total ingestion size: {}", requests.size());

    return Flux.fromIterable(requests)
        // Splitting into micro-batches of 1000
        .buffer(BATCH_SIZE)
        // Sending multiple batches in parallel with limited concurrency
        .flatMap(batch -> sendBatch(key, batch, cb), MAX_PARALLEL_BATCHES).then();
  }

  private Mono<Void> sendBatch(String key, List<MessageRequest> batch, CircuitBreaker cb) {
    log.info("Sending sub-batch of size {}", batch.size());

    return Flux.fromIterable(batch).map(request -> createSenderRecord(request, key))
        .as(kafkaSender::send)
        // Applying CircuitBreaker to the whole micro-batch
        .transformDeferred(CircuitBreakerOperator.of(cb))
        // Collecting stats per batch
        .collectList().doOnNext(results -> {
          long success = results.stream().filter(r -> r.exception() == null).count();
          long failed = results.size() - success;
          log.info("Sub-batch complete: {} succeeded, {} failed", success, failed);

          // Optionally trigger CircuitBreaker if all failed
          if (failed == results.size()) {
            throw new RuntimeException("All records in batch failed");
          }
        }).then();
  }

  private SenderRecord<String, Object, MessageRequest> createSenderRecord(MessageRequest request,
      String key) {
    // Using traceId from the request as key for partitioning
    ProducerRecord<String, Object> record = new ProducerRecord<>(TOPIC, key, request);
    return SenderRecord.create(record, request);
  }

}
