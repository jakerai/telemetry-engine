package com.telemetry.engine.query.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import com.telemetry.engine.query.websocket.AssetTrackingWsHandler;
import com.telemetry.engine.query.websocket.NearbyAssetsWsHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

  private final AssetTrackingWsHandler assetTrackingWsHandler;
  private final NearbyAssetsWsHandler nearbyAssetsWsHandler;

  @Bean
  public HandlerMapping webSocketHandlerMapping() {

    Map<String, WebSocketHandler> map = new HashMap<>();
    map.put("/ws/nearby-assets", nearbyAssetsWsHandler);
    map.put("/ws/asset-tracking/{assetId}", assetTrackingWsHandler);

    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setUrlMap(map);
    /* Order must be higher priority than standard controllers to intercept WS */
    mapping.setOrder(-1);
    return mapping;
  }

  /**
   * Adapter to enable WebFlux WebSocket support
   */
  @Bean
  public WebSocketHandlerAdapter handlerAdapter() {
    return new WebSocketHandlerAdapter();
  }


}
