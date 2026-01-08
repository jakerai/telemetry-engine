package com.telemetry.engine.gateway.security.apikey.filter;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.common.constansts.ContextConstants;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.utils.ResponseBuilder;
import com.telemetry.engine.gateway.security.apikey.service.ApiKeyAuthService;
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
      log.debug("[ApiKeyAuthGatewayFilterFactory.apply] Validating request using api key");
      String apiKey = exchange.getRequest().getHeaders().getFirst("X-Api-Key");

      if (apiKey == null || apiKey.isBlank()) {
        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing API Key");
      }

      return apiKeyAuthService.isValid(apiKey).flatMap(meta -> {
        log.debug("Validation is {}", meta.isValid());
        if (Boolean.TRUE.equals(meta.isValid())) {

          String userId = meta.getUserId() != null ? String.valueOf(meta.getUserId()) : "0";
          String requestId =
              Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Request-Id"))
                  .orElse(UUID.randomUUID().toString());
          String clientIp =
              Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                  .orElseGet(() -> {
                    if (exchange.getRequest().getRemoteAddress() != null) {
                      return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                    }
                    return "unknown";
                  });

          // propagating per-request information downstream
          ServerHttpRequest mutatedRequest =
              exchange.getRequest().mutate().header("X-User-Id", userId)
                  .header("X-Request-Id", requestId).header("X-Client-Ip", clientIp).build();

          return chain.filter(exchange.mutate().request(mutatedRequest).build())
              .contextWrite(ctx -> ctx.put(ContextConstants.CONTEXT_USER_ID, userId)
                  .put(ContextConstants.CONTEXT_REQUEST_ID, requestId));
        } else {
          log.warn("Invalid API Key attempt");
          return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid API Key");
        }
      }).switchIfEmpty(Mono
          .defer(() -> writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid API Key")));
    };
  }


  private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status,
      String message) {
    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    ServiceResponse<?> responseStatus =
        ResponseBuilder.error(message, exchange.getResponse().getStatusCode().value());

    try {
      byte[] bytes = objectMapper.writeValueAsBytes(responseStatus);
      return exchange.getResponse()
          .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    } catch (Exception e) {
      return exchange.getResponse().setComplete(); // fallback if serialization fails
    }
  }

  // Adding Internal Config class to avoid importing from other filters
  public static class Config {
    // Add fields here if you want to pass args from the route definition
  }

}
