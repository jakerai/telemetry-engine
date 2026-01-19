package com.telemetry.engine.query.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import com.telemetry.engine.common.redis.RedisService;
import com.telemetry.engine.query.service.AssetService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetTrackingWsHandler implements WebSocketHandler {

  private final RedisService redisService;
  private final AssetService assetService;
  
  @Override
  public Mono<Void> handle(WebSocketSession session) {
   return null;
  }

}
