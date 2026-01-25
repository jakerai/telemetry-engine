package com.telemetry.engine.ingestion.service;

import java.util.List;
import com.telemetry.engine.common.dto.MessageEvent;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import reactor.core.publisher.Mono;

public interface IngestionService {

  Mono<ServiceResponse<Void>> ingest(List<MessageEvent> events);

}
