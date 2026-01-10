package com.telemetry.engine.gateway.security.jwt;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import com.telemetry.engine.common.constansts.ContextConstants;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthGatewayFilterFactory
    extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

  public JwtAuthGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    log.debug("Validating request using jwt");
    return (exchange, chain) -> {
      Mono<Void> filterMono;

      filterMono = exchange.getPrincipal()
          .cast(JwtAuthenticationToken.class)
          .flatMap(auth -> {

            String userId = auth.getToken().getClaimAsString("userId") != null
                ? auth.getToken().getClaimAsString("userId")
                : "0";

            String clientIp =
                Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                    .orElseGet(() -> exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown");

            // propagating info downstream
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Id", userId)
                .header("X-Client-Ip", clientIp)
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(ctx -> ctx
                    .put(ContextConstants.CONTEXT_USER_ID, userId)
                    .put(ContextConstants.CONTEXT_CLIENT_IP, clientIp));
          });

      return filterMono.switchIfEmpty(chain.filter(exchange));
    };
  }

  public static class Config {
  }

}
