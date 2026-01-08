package com.telemetry.engine.gateway.security.jwt;

import java.util.Optional;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import com.telemetry.engine.common.constansts.ContextConstants;

@Component
public class JwtAuthGatewayFilterFactory
    extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

  public JwtAuthGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> exchange.getPrincipal().cast(JwtAuthenticationToken.class)
        .flatMap(auth -> {

          String userId = auth.getToken().getClaimAsString("userId") != null
              ? auth.getToken().getClaimAsString("userId")
              : "0";
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

        }).switchIfEmpty(chain.filter(exchange));
  }

  public static class Config {
  }

}
