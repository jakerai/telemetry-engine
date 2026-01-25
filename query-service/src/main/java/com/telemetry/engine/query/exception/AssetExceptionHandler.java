package com.telemetry.engine.query.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import com.telemetry.engine.common.context.RequestContext;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class AssetExceptionHandler {

  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<ServiceResponse<Map<String, String>>>> handleValidationErrors(
          WebExchangeBindException ex) {

      Map<String, String> errors = new HashMap<>();
      ex.getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

      ServiceResponse<Map<String, String>> response =
              ServiceResponse.<Map<String, String>>builder()
                      .success(false)
                      .status(HttpStatus.BAD_REQUEST.value())
                      .message("Validation Failed")
                      .errors(errors)
                      .timestamp(Instant.now())
                      .requestId(RequestContext.getTraceId())
                      .build();

      return Mono.just(ResponseEntity.badRequest().body(response));
  }
  
  /* Body parsing / deserialization errors */
  @ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<ServiceResponse<Void>>> handleWebInputException(
      ServerWebInputException ex) {
    ServiceResponse<Void> response = ServiceResponse.<Void>builder().success(false)
        .status(ex.getStatusCode().value()).message("Validation Failed").errors(ex.getReason())
        .timestamp(Instant.now()).requestId(RequestContext.getTraceId()).build();

    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(response));
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
