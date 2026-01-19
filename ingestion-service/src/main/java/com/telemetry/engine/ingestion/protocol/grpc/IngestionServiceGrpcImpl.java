package com.telemetry.engine.ingestion.protocol.grpc;

import java.util.List;
import org.springframework.grpc.server.service.GrpcService;
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
  public void ingest(ServiceRequest request, StreamObserver<ServiceResponse> responseObserver) {

    log.error("Request received: {}", 
        request);
        
    try {
      MessageEventList events = request.getPayload();

      List<com.telemetry.engine.ingestion.dto.MessageEvent> messageEvents =
          events.getEventsList().stream().map(this::toDomainDto).toList();

      var domainRequest =
          com.telemetry.engine.common.dto.request.ServiceRequest.<List<com.telemetry.engine.ingestion.dto.MessageEvent>>builder()
              .payload(messageEvents).build();

      ingestionService.ingest(domainRequest).doOnNext(resp -> {
        if (!resp.getStatus().isSuccess()) {
          log.warn("Ingestion failed: {}", resp.getStatus().getMessage());
        }
      }).subscribe(resp -> {

        com.telemetry.engine.ingestion.protocol.grpc.ServiceResponse grpcResp =
            mapToGrpcResponse(resp);

        responseObserver.onNext(grpcResp);
      }, err -> {
        log.error("Fatal error in gRPC ingestion", err);
        responseObserver.onError(Status.INTERNAL.withCause(err).asRuntimeException());
      }, () -> responseObserver.onCompleted());

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }



  }


  private com.telemetry.engine.ingestion.dto.MessageEvent toDomainDto(MessageEvent grpcEvent) {
    return com.telemetry.engine.ingestion.dto.MessageEvent.builder()
        .requestId(grpcEvent.getRequestId()).assetId(grpcEvent.getAssetId())
        .latitude(grpcEvent.getLatitude()).longitude(grpcEvent.getLongitude())
        .speed(grpcEvent.getSpeed()).heading(grpcEvent.getHeading())
        .deviceTs(grpcEvent.hasDeviceTs() ? java.time.Instant.ofEpochSecond(
            grpcEvent.getDeviceTs().getSeconds(), grpcEvent.getDeviceTs().getNanos()) : null)
        .build();
  }

  private com.telemetry.engine.ingestion.protocol.grpc.ServiceResponse mapToGrpcResponse(
      com.telemetry.engine.common.dto.response.ServiceResponse<Void> domainResp) {

    com.telemetry.engine.common.dto.response.ResponseStatus status = domainResp.getStatus();

    return com.telemetry.engine.ingestion.protocol.grpc.ServiceResponse.newBuilder()
        .setStatus(com.telemetry.engine.ingestion.protocol.grpc.ResponseStatus.newBuilder()
            .setSuccess(status.isSuccess())
            .setMessage(status.getMessage() == null ? "" : status.getMessage())
            .setStatus(status.getStatus())
            .setRequestId(status.getRequestId() == null ? "" : status.getRequestId()).build())
        .build();
  }

}
