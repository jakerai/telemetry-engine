package com.telemetry.engine.ingestion.protocol.rsocket;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.ingestion.dto.MessageEvent;
import com.telemetry.engine.ingestion.protocol.grpc.MessageEventListGrpc;
import com.telemetry.engine.ingestion.protocol.grpc.MessageEventRequestGrpc;
import com.telemetry.engine.ingestion.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IngestionRSocketController {

  private final IngestionService ingestionService;

  /**
   * Fire-and-Forget ingestion using a list of events for high-frequency telemetry from devices that
   * batch events.
   */
  @MessageMapping("ingest.fireAndForget")
  public Mono<Void> ingestFireAndForget(MessageEventListGrpc batch) {
    List<MessageEvent> events = batch.getEventsList().stream().map(this::toMessageEvent).toList();

    return ingestionService.ingest(events)
        .doOnSuccess(resp -> log.info("FF ingestion success for {} events, requestId={}",
            events.size(), resp.requestId()))
        .doOnError(err -> log.error("FF ingestion failed", err)).then();
  }

  /**
   * Request-Channel ingestion: receives a stream of event batches. Supports backpressure, batching,
   * and concurrency control.
   */
  @MessageMapping("ingest.stream")
  public Flux<ServiceResponse<Void>> ingestStream(Flux<MessageEventListGrpc> batchStream) {
    return batchStream
        /* Handling back pressure buffer up to 100 batches */
        .onBackpressureBuffer(100,
            dropped -> log.warn("Dropped batch due to backpressure: {}", dropped))

        /* Convert each batch to List<MessageEvent> */
        .flatMap(batch -> {
          List<MessageEvent> events =
              batch.getEventsList().stream().map(this::toMessageEvent).toList();
          return ingestionService.ingest(events);
        }, 4) /* Process max 4 batches in parallel */

        /* Returning a success response per batch */
        .map(resp -> ServiceResponse.<Void>success(null,
            "Ingested batch, requestId=" + Objects.requireNonNullElse(resp.requestId(), "N/A")))

        /* Log errors but continue processing */
        .doOnError(err -> log.error("Stream ingestion error", err))
        .onErrorContinue((err, obj) -> log.warn("Skipping malformed batch: {}", err.getMessage()));
  }


  /**
   * Map a single MessageEventRequestGrpc to your internal DTO.
   */
  private MessageEvent toMessageEvent(MessageEventRequestGrpc grpcEvent) {
    return MessageEvent.builder().requestId(grpcEvent.getRequestId())
        .assetId(grpcEvent.getAssetId()).latitude(grpcEvent.getLatitude())
        .longitude(grpcEvent.getLongitude()).speed(grpcEvent.getSpeed())
        .heading(grpcEvent.getHeading())
        .deviceTs(
            grpcEvent.hasDeviceTs() ? Instant.ofEpochSecond(grpcEvent.getDeviceTs().getSeconds(),
                grpcEvent.getDeviceTs().getNanos()) : null)
        .build();
  }

}
