package com.telemetry.engine.query.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import com.telemetry.engine.common.mapper.JsonMapperUtil;
import com.telemetry.engine.query.dto.resquest.NearbyAssetsRequest;
import com.telemetry.engine.query.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class NearbyAssetsWsHandler implements WebSocketHandler {

  private final AssetService assetService;
  private final JsonMapperUtil mapperService;


  @Override
  public Mono<Void> handle(WebSocketSession session) {
    return session.receive().next() // Getting the first message containing the lat/lon/radius
        .map(msg -> mapperService.deserializeFromJson(msg.getPayloadAsText(),
            NearbyAssetsRequest.class))
        .flatMap(req -> {

          // Stream nearby assets: snapshot + deltas + heartbeat
          Flux<WebSocketMessage> responseStream = assetService
              .getNearbyAssets(req.getLat(), req.getLon(), req.getAssetType(),
                  (int) req.getRadiusMeters())
              .map(event -> mapperService.serializeToJson(event)) /* Map to JSON string */
              .map(session::textMessage);

          /* Sending the stream to the WebSocket session */
          return session.send(responseStream)
              .doOnCancel(() -> log.info("WebSocket session cancelled"));
        }).onErrorResume(e -> {
          e.printStackTrace();
          return session.close();
        });
  }


}
