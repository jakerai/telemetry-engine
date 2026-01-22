package com.telemetry.engine.gateway.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class GatewayResponseBuilder {
  private GatewayResponseBuilder() {}

  public static Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status,
      String message, ObjectMapper objectMapper) {
    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    ServiceResponse<?> response = ServiceResponse.builder().message(message)
        .status(exchange.getResponse().getStatusCode().value()).build();

    try {
      byte[] bytes = objectMapper.writeValueAsBytes(response);
      return exchange.getResponse()
          .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    } catch (Exception e) {
      log.error("Error writing gateway error response", e);
      return exchange.getResponse().setComplete();
    }
  }

}
