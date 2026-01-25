package com.telemetry.engine.ingestion.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.telemetry.engine.common.constansts.ContextConstants;
import com.telemetry.engine.common.context.RequestContext;
import com.telemetry.engine.common.dto.MessageEvent;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.ingestion.config.CircuitBreakerManager;
import com.telemetry.engine.ingestion.producer.IngestionProducer;
import com.telemetry.engine.ingestion.service.IngestionService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionServiceImpl implements IngestionService {

  private final IngestionProducer ingestionProducer;
  private final CircuitBreakerManager cbManager;

  public Mono<ServiceResponse<Void>> ingest(List<MessageEvent> messageEvents) {
    log.info("Ingesting {} messages...", messageEvents.size());

    return Mono.deferContextual(ctx -> {

      Long userId = ctx.get(ContextConstants.CONTEXT_USER_ID);
      log.info("Retrieved user ID={} from context", userId);
      String traceId = RequestContext.getTraceId();

      CircuitBreaker cb = cbManager.getOrCreate("kafkaIngestCB");

      return ingestionProducer.send(traceId, messageEvents, cb)
          .thenReturn(ServiceResponse.<Void>success("Ingested successfully")).onErrorResume(e -> {
            log.error("Ingestion failed for user ID={}", userId, e);
            return Mono.just(ServiceResponse.<Void>error("Failed to ingest data"));
          });
    });
  }


}
