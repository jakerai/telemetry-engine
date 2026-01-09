package com.telemetry.engine.ingestion.controller.v1;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.ingestion.dto.MessageRequest;
import com.telemetry.engine.ingestion.service.IngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/v1/ingest")
@RequiredArgsConstructor
public class IngestionController {

  private final IngestionService ingestionService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ServiceResponse<Void>> ingest(
      @RequestBody @Valid ServiceRequest<List<MessageRequest>> serviceRequest) {

    return ingestionService.ingest(serviceRequest);
  }

}
