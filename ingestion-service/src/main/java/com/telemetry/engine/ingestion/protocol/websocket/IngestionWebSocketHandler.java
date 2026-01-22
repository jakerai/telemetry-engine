package com.telemetry.engine.ingestion.protocol.websocket;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import com.telemetry.engine.common.mapper.JsonMapperUtil;
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

  private final IngestionService ingestionService;

  @Override
  public Mono<Void> handle(WebSocketSession session) {

    String userId = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");
    log.info("User ID={} extracted from header", userId);

    return session.receive()
        // Only handle text messages
        .filter(msg -> msg.getType() == WebSocketMessage.Type.TEXT)
        .map(WebSocketMessage::getPayloadAsText).filter(text -> !text.isBlank())

        // Parse JSON off main event loop
        .flatMap(json -> toMessageEvents(json).subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
              log.warn("Failed to parse incoming message: {}", e.getMessage());
              return Mono.empty(); // skip malformed JSON
            }))

        // Process ingestion
        .flatMap(events -> ingestionService.ingest(events)
            .doOnSuccess(v -> log.info("Successfully ingested {} messages for user ID={}",
                events.size(), userId))
            .doOnError(e -> log.error("Ingestion failed for user ID={}", userId, e))
            .thenReturn("Ingested")) // simple message for client
        // Serialize response to JSON
        .flatMap(respMessage -> Mono.fromCallable(() -> JsonMapperUtil.serializeToJson(respMessage))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(jsonResp -> session.send(Mono.just(session.textMessage(jsonResp))))
            .onErrorResume(e -> {
              log.error("Failed to serialize/send response", e);
              return Mono.empty();
            }))

        .doOnError(err -> log.error("Fatal error in WebSocket stream for session: {}",
            session.getId(), err))
        .onErrorContinue((err, obj) -> log.warn("Skipping malformed message: {}", err.getMessage()))
        .then();
  }

  private Mono<List<MessageEvent>> toMessageEvents(String jsonArray) {

    return Mono
        .fromCallable(() -> JsonMapperUtil.deserializeJsonToList(jsonArray, MessageEvent.class));
  }

}
