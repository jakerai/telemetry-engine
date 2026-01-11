package com.telemetry.engine.consumer.service.impl;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.consumer.dto.MessageEvent;
import com.telemetry.engine.consumer.entity.AssetEvent;
import com.telemetry.engine.consumer.metrics.DailyCounter;
import com.telemetry.engine.consumer.repository.AssetEventRepository;
import com.telemetry.engine.consumer.service.Processor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorImpl implements Processor {

  private final ObjectMapper objectMapper;
  private final AssetEventRepository repository;

  private final DailyCounter dailyCounter;

  @Override
  public Mono<Void> process(String jsonArrayValue) {

    // Deserializing JSON array on boundedElastic (blocking)
    return Mono
        .fromCallable(() -> objectMapper.readValue(jsonArrayValue,
            new TypeReference<List<MessageEvent>>() {}))
        .subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromIterable)
        .map(this::extractFields)
        // Saving all events reactively in DB
        .as(repository::saveAll) // returns Flux<AssetEvent>
        .reduce(0, (count, ignored) -> count + 1).doOnSuccess(dailyCounter::increment).then() // converting Flux<AssetEvent> to Mono<Void>
        .doOnError(e -> log.error("Failed to process and save batch", e));
  }

  /**
   * Extracting and modifying fields from the event before saving
   */
  private AssetEvent extractFields(MessageEvent event) {

    event.setProcessedAt(Instant.now());
    return AssetEvent.builder().assetId(event.getAssetId()).longitude(event.getLatitude())
        .latitude(event.getLatitude()).heading(event.getHeading())
        .processedAt(event.getProcessedAt()).build();
  }

}
