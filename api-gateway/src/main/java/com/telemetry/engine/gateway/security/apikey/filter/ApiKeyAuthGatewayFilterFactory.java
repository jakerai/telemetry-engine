package com.telemetry.engine.gateway.security.apikey.filter;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.gateway.security.apikey.service.ApiKeyAuthService;
import com.telemetry.engine.gateway.util.GatewayResponseBuilder;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyAuthGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ApiKeyAuthGatewayFilterFactory.Config> {
  private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthGatewayFilterFactory.class);

  private final ApiKeyAuthService apiKeyAuthService;
  private final ObjectMapper objectMapper;

  public ApiKeyAuthGatewayFilterFactory(ApiKeyAuthService apiKeyAuthService,
      ObjectMapper objectMapper) {
    super(Config.class);
    this.apiKeyAuthService = apiKeyAuthService;
    this.objectMapper = objectMapper;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {

      log.debug("Validating request using API key");
      String apiKey = exchange.getRequest().getHeaders().getFirst("X-Api-Key");

      Mono<ServerWebExchange> mutatedExchangeMono;

      if (apiKey == null || apiKey.isBlank()) {
        mutatedExchangeMono = Mono.just(exchange).flatMap(ex -> GatewayResponseBuilder
            .writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing API Key", objectMapper)
            .then(Mono.empty()));
      } else {
        mutatedExchangeMono = apiKeyAuthService.isValid(apiKey).flatMap(meta -> {
          if (Boolean.TRUE.equals(meta.isValid())) {
            String userId = meta.getUserId() != null ? String.valueOf(meta.getUserId()) : "0";
            String clientIp =
                Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                    .orElseGet(() -> exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown");

            // propagating info downstream
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId).header("X-Client-Ip", clientIp).build();

            return Mono.just(exchange.mutate().request(mutatedRequest).build());
          } else {
            // invalid key mark for 401
            return GatewayResponseBuilder.writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED,
                "Invalid API Key", objectMapper).then(Mono.empty());
          }
        }).switchIfEmpty(Mono.defer(() -> GatewayResponseBuilder
            .writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid API Key", objectMapper)
            .then(Mono.empty())));
      }

      return mutatedExchangeMono.flatMap(mutatedExchange -> chain.filter(mutatedExchange));
    };
  }



  // Adding Internal Config class to avoid importing from other filters
  public static class Config {
    // we can add fields here if we want to pass args from the route definition
  }

}
