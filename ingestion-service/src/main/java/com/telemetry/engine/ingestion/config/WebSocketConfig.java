package com.telemetry.engine.ingestion.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import com.telemetry.engine.ingestion.protocol.websocket.IngestionWebSocketHandler;

@Configuration
public class WebSocketConfig {

  /**
   * Map WebSocket endpoint to handler. /ws/location will be your WebSocket URL for drivers.
   */
  @Bean
  public HandlerMapping handlerMapping(IngestionWebSocketHandler handler) {
    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setUrlMap(Map.of("/ws/v1/ingest", handler));
    mapping.setOrder(10);
    return mapping;
  }

  /**
   * Adapter to enable WebFlux WebSocket support.
   */
  @Bean
  public WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter();
  }

}
