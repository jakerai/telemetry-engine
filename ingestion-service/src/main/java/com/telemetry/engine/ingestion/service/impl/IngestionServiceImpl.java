package com.telemetry.engine.ingestion.service.impl;

import org.springframework.stereotype.Service;
import com.telemetry.engine.common.constansts.ContextConstants;
import com.telemetry.engine.common.constansts.StatusCode;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.utils.ResponseBuilder;
import com.telemetry.engine.ingestion.dto.IngestionRequest;
import com.telemetry.engine.ingestion.kafka.producer.IngestionProducer;
import com.telemetry.engine.ingestion.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionServiceImpl implements IngestionService {

  private final IngestionProducer ingestionProducer;

  @Override
  public Mono<ServiceResponse<Void>> ingest(ServiceRequest<IngestionRequest> serviceRequest) {
    log.info("[IngestionServiceImpl.ingest] Ingesting data...");
    IngestionRequest payload = serviceRequest.getPayload();
    return Mono.deferContextual(ctx -> {
      
      Long userId = ctx.getOrDefault(ContextConstants.CONTEXT_USER_ID, 0L);
      
      return ingestionProducer.send(payload)
          .thenReturn(ResponseBuilder.<Void>success("Ingested successfully")).onErrorResume(e -> {
            log.error("Ingestion failed for user: {}", userId, e);
            return Mono
                .just(ResponseBuilder.<Void>error("Failed to ingest data", StatusCode.FAILED));
          });
    });
  }

}
