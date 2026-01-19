package com.telemetry.engine.ingestion.dto;

import java.time.Instant;
import com.telemetry.engine.common.context.RequestContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageEvent {

  @Builder.Default
  private String requestId = RequestContext.getTraceId();
  private Long assetId; 
  private double latitude;
  private double longitude;
  private double speed;
  private double heading;
  private Instant deviceTs;
  @Builder.Default
  private Instant processedAt = Instant.now();

}
