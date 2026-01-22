package com.telemetry.engine.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

  @GetMapping("/auth")
  public Mono<ResponseEntity<ServiceResponse<?>>> authFallback() {
    return createFallbackResponse("Authentication service");
  }

  @GetMapping("/jwt")
  public Mono<ResponseEntity<ServiceResponse<?>>> jwtFallback() {
    return createFallbackResponse("JWT service");
  }

  @GetMapping("/apikey")
  public Mono<ResponseEntity<ServiceResponse<?>>> apiKeyFallback() {
    return createFallbackResponse("API Key service");
  }

  private Mono<ResponseEntity<ServiceResponse<?>>> createFallbackResponse(String serviceName) {

    ServiceResponse<?> response = ServiceResponse.builder().status(HttpStatus.SERVICE_UNAVAILABLE.value())
        .message(serviceName + " is currently unavailable.").build();

    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
  }

}
