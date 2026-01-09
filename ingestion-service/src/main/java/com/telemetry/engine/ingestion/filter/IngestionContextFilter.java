package com.telemetry.engine.ingestion.filter;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import com.telemetry.engine.common.constansts.ContextConstants;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class IngestionContextFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
 
    String userId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
            .orElse("0L");
    String requestId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Request-Id"))
            .orElse(UUID.randomUUID().toString());
    String clientIp = exchange.getRequest().getRemoteAddress() != null
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";

    if ("anonymous".equals(userId)) {
        log.warn("Missing X-User-Id header from ip={}", clientIp);
    }

    return chain.filter(exchange)
            .contextWrite(ctx -> ctx
                    .put(ContextConstants.CONTEXT_USER_ID, userId)
                    .put(ContextConstants.CONTEXT_REQUEST_ID, requestId)
                    .put(ContextConstants.CONTEXT_CLIENT_IP, clientIp)
            );
}
  
}
