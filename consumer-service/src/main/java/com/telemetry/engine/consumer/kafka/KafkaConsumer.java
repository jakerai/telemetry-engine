package com.telemetry.engine.consumer.kafka;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import com.telemetry.engine.consumer.config.CircuitBreakerManager;
import com.telemetry.engine.consumer.service.Processor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

  private final KafkaReceiver<String, String> kafkaReceiver; // JSON array strings
  private final Processor processor;
  private final CircuitBreakerManager circuitBreakerManager;

  private Disposable subscription;
  private final AtomicInteger inFlight = new AtomicInteger(0);

  public void consume() {
      CircuitBreaker cb = circuitBreakerManager.getOrCreate("ingestionConsumer");

      log.info("Kafka Consumer starting...");

      subscription = kafkaReceiver.receive()
          .doOnSubscribe(s -> log.info("Kafka Consumer subscribed"))
          .doOnNext(record -> log.info("Received message from Kafka. partition={}, offset={}, key={}", 
              record.partition(), record.offset(), record.key()))
          .doOnError(e -> log.error("Kafka Consumer stream error", e))
          .doOnCancel(() -> log.warn("Kafka Consumer cancelled"))
          .doOnComplete(() -> log.info("Kafka Consumer completed"))
          .flatMap(record -> handleRecord(record, cb))
          .subscribe();

      log.info("Kafka Consumer started");
  }

  private Mono<Void> handleRecord(ReceiverRecord<String, String> record, CircuitBreaker cb) {
      return Mono.defer(() -> {
          inFlight.incrementAndGet();

          String jsonArrayValue = record.value();

          return processor.process(jsonArrayValue)
              .transformDeferred(CircuitBreakerOperator.of(cb))
              .doOnSuccess(v -> {
                  record.receiverOffset().acknowledge(); // manual commit
                  log.info("Successfully processed batch. partition={}, offset={}",
                          record.partition(), record.offset());
              })
              .doOnError(e -> log.error("Failed processing batch. partition={}, offset={}",
                      record.partition(), record.offset(), e))
              .onErrorResume(e -> Mono.empty())
              .doFinally(signal -> {
                  int remaining = inFlight.decrementAndGet();
                  log.debug("In-flight batches remaining: {}", remaining);
              });
      });
  }

  /**
   * Graceful shutdown: stop consuming new records, wait for in-flight to finish.
   */
  @PreDestroy
  public void shutdown() {
      log.info("Kafka Consumer stopping...");

      if (subscription != null && !subscription.isDisposed()) {
          subscription.dispose();
          log.info("Kafka Consumer subscription disposed");
      }

      while (inFlight.get() > 0) {
          log.info("Waiting for {} in-flight batches to finish...", inFlight.get());
          try {
              Thread.sleep(500);
          } catch (InterruptedException ignored) {
          }
      }

      log.info("All in-flight batches processed. Kafka Consumer stopped.");
  }
  
  
}
