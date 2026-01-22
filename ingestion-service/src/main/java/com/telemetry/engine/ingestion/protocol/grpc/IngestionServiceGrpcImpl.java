package com.telemetry.engine.ingestion.protocol.grpc;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.grpc.server.service.GrpcService;
import com.google.protobuf.Timestamp;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.ingestion.dto.MessageEvent;
import com.telemetry.engine.ingestion.service.IngestionService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class IngestionServiceGrpcImpl extends IngestionServiceGrpc.IngestionServiceImplBase {

  private final IngestionService ingestionService;

  @Override
  public void ingestGrpc(ServiceRequestGrpc request,
      StreamObserver<ServiceResponseGrpc> responseObserver) {

    try {
      MessageEventListGrpc eventListGrpc = request.getPayload();
      log.info("RECEIVED={}", request);
      
      List<MessageEvent> messageEvents =
          eventListGrpc.getEventsList().stream().map(this::toMessageEvent).toList();

      ingestionService.ingest(messageEvents).subscribe(resp -> {
        responseObserver.onNext(mapToGrpcResponse(resp));
        responseObserver.onCompleted();
      }, err -> {
        log.error("gRPC ingestion failed", err);
        responseObserver.onError(Status.INTERNAL.withDescription("Ingestion failed").withCause(err)
            .asRuntimeException());
      });

    } catch (Exception e) {
      log.error("Synchronous error before reactive pipeline", e);
      responseObserver.onError(Status.INTERNAL.withDescription("Invalid gRPC request").withCause(e)
          .asRuntimeException());
    }
  }

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

  private ServiceResponseGrpc mapToGrpcResponse(ServiceResponse<Void> response) {
    return ServiceResponseGrpc.newBuilder().setSuccess(response.success())
        .setMessage(Objects.requireNonNullElse(response.message(), "")).setStatus(response.status())
        .setRequestId(Objects.requireNonNullElse(response.requestId(), ""))
        .setTimestamp(toProtoTimestamp(response.timestamp())).build();
  }

  private Timestamp toProtoTimestamp(Instant instant) {
    if (instant == null) {
      return com.google.protobuf.Timestamp.getDefaultInstance();
    }
    return com.google.protobuf.Timestamp.newBuilder().setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano()).build();
  }

}
