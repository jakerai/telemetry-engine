package com.telemetry.engine.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {
  @GetMapping("/fallback/jwt")
  public Mono<String> jwtFallback() {
      return Mono.just("JWT service unavailable. Try again later.");
  }

  @GetMapping("/fallback/apikey")
  public Mono<String> apiKeyFallback() {
      return Mono.just("API Key service unavailable. Try again later.");
  }
}
