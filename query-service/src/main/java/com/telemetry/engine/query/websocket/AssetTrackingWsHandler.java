package com.telemetry.engine.query.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetTrackingWsHandler implements WebSocketHandler {

    
  @Override
  public Mono<Void> handle(WebSocketSession session) {
   return null;
  }

}
