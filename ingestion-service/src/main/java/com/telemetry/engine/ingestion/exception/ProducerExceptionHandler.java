package com.telemetry.engine.ingestion.exception;

import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import com.telemetry.engine.common.context.RequestContext;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import reactor.core.publisher.Mono;


/**
 * @author Vishal Rai
 */

@RestControllerAdvice
public class ProducerExceptionHandler {


  /* Body parsing / deserialization errors */
  @ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<ServiceResponse<Void>>> handleWebInputException(
      ServerWebInputException ex) {
    ServiceResponse<Void> response = ServiceResponse.<Void>builder().success(false)
        .status(ex.getStatusCode().value()).message("Validation Failed").errors(ex.getReason())
        .timestamp(Instant.now()).requestId(RequestContext.getTraceId()).build();

    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(response));
  }

  /* Validation errors (binding errors) */
  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<ServiceResponse<Void>>> handleValidationErrors(
      WebExchangeBindException ex) {
    String firstError = ex.getBindingResult().getFieldErrors().isEmpty() ? "Invalid request"
        : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

    ServiceResponse<Void> response = ServiceResponse.<Void>builder().success(false).status(400)
        .message("Validation Failed: " + firstError).timestamp(Instant.now())
        .requestId(RequestContext.getTraceId()).build();

    return Mono.just(ResponseEntity.badRequest().body(response));
  }

  /* Unsupported Media Type */
  @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
  public Mono<ResponseEntity<ServiceResponse<Void>>> handleUnsupportedMediaType(
      UnsupportedMediaTypeStatusException ex) {
    String supported = ex.getSupportedMediaTypes().isEmpty() ? "None"
        : String.join(", ", ex.getSupportedMediaTypes().stream().map(Object::toString).toList());

    ServiceResponse<Void> response =
        ServiceResponse.<Void>builder().success(false).status(ex.getStatusCode().value())
            .message("Unsupported Content-Type. Supported types: " + supported)
            .timestamp(Instant.now()).requestId(RequestContext.getTraceId()).build();

    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(response));
  }


}
