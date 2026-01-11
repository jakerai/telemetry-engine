package com.telemetry.engine.ingestion.service;

import java.util.List;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.ingestion.dto.MessageEvent;
import reactor.core.publisher.Mono;

public interface IngestionService {

  Mono<ServiceResponse<Void>> ingest(ServiceRequest<List<MessageEvent>> serviceRequest);

}
