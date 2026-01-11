package com.telemetry.engine.ingestion.exception;

import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import com.telemetry.engine.common.dto.response.ResponseStatus;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import reactor.core.publisher.Mono;


/**
 * @author Vishal Rai
 */

@RestControllerAdvice
public class ProducerExceptionHandler {

    
 // Handles body parsing / deserialization errors
  @ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<ResponseStatus>> handleWebInputException(ServerWebInputException ex) {
      ResponseStatus status = ResponseStatus.builder()
              .success(false)
              .message("Validation Failed")
              .status(ex.getStatusCode().value())
              .errors(ex.getReason())
              .timestamp(Instant.now())
              .build();
      return Mono.just(ResponseEntity.status(status.getStatus()).body(status));
  }


  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<ServiceResponse<Void>>> handleValidationDocs(WebExchangeBindException ex) {
      ServiceResponse<Void> response = ServiceResponse.<Void>builder()
          .status(ResponseStatus.builder()
              .status(ex.getStatusCode().value())
              .message("Validation Failed: " + ex.getBindingResult().getFieldError().getDefaultMessage())
              .build())
          .build();
      return Mono.just(ResponseEntity.badRequest().body(response));
  }
  
  @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
  public Mono<ResponseEntity<ResponseStatus>> handleUnsupportedMediaType(UnsupportedMediaTypeStatusException ex) {

      String supported = ex.getSupportedMediaTypes().isEmpty() ? "None" :
              String.join(", ", ex.getSupportedMediaTypes().stream().map(Object::toString).toList());

      ResponseStatus status = ResponseStatus.builder()
              .success(false)
              .message("Validation Failed")
              .status(ex.getStatusCode().value())
              .errors("Unsupported Content-Type. Supported types: " + supported)
              .timestamp(Instant.now())
              .build();

      return Mono.just(ResponseEntity.status(status.getStatus()).body(status));
  }



}
