package com.telemetry.engine.ingestion.protocol.websocket;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.ingestion.dto.MessageEvent;
import com.telemetry.engine.ingestion.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionWebSocketHandler implements WebSocketHandler {

  private final ObjectMapper mapper;
  private final IngestionService ingestionService;

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    
    String userId = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");
    log.info("User ID={} extracted from header", userId);
    return session.receive()
        /* Filtering out empty frames/pings */
        .filter(msg -> msg.getType() == WebSocketMessage.Type.TEXT)
        .map(WebSocketMessage::getPayloadAsText).filter(text -> !text.isBlank())

        /* Parsing JSON off the main event loop */
        .flatMap(json -> toMessageEvents(json).subscribeOn(Schedulers.boundedElastic()))

        .flatMap(events -> {
          ServiceRequest<List<MessageEvent>> request = new ServiceRequest<>();
          request.setPayload(events);

          return ingestionService.ingest(request).doOnNext(resp -> {
            if (!resp.getStatus().isSuccess()) {
              log.warn("Ingestion failed: {}", resp.getStatus().getMessage());
            }
          }).flatMap(resp -> {
            try {
              /* Sending response back to client */
              String jsonResp = mapper.writeValueAsString(resp);
              return session.send(Mono.just(session.textMessage(jsonResp)));
            } catch (Exception e) {
              log.error("Error serializing response", e);
              return Mono.empty();
            }
          });
        })
        .doOnError(err -> log.error("Fatal error in WebSocket stream for session: {}",
            session.getId(), err))
        .onErrorContinue(
            (err, obj) -> log.error("Skipping malformed message: {}", err.getMessage()))
        .then();
  }

  private Mono<List<MessageEvent>> toMessageEvents(String jsonArray) {
    return Mono.fromCallable(
        () -> mapper.readValue(jsonArray, new TypeReference<List<MessageEvent>>() {}));
  }

}
