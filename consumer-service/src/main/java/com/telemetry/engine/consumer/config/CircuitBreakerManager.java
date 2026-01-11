package com.telemetry.engine.consumer.config;

import java.time.Duration;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
public class CircuitBreakerManager {

  private final CircuitBreakerRegistry registry;

  public CircuitBreakerManager(CircuitBreakerRegistry registry) {
    this.registry = registry;
  }

  /**
   * Getting existing CB or create a new one using the registry's internal cache
   */
  public CircuitBreaker getOrCreate(String name) {
    return registry.circuitBreaker(name, getDefaultConfig());
  }

  private CircuitBreakerConfig getDefaultConfig() {
    return CircuitBreakerConfig.custom().failureRateThreshold(50).slidingWindowSize(20)
        .waitDurationInOpenState(Duration.ofSeconds(10)).permittedNumberOfCallsInHalfOpenState(5)
        .build();
  }

}
