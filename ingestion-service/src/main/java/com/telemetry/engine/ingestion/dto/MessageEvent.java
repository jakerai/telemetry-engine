package com.telemetry.engine.ingestion.dto;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.telemetry.engine.common.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class MessageEvent {
  
  private String requestId;
  private Long assetId;
  private Long assetTypeId;
  private Long operatorId;
  private double longitude;
  private double latitude;
  private double speed;
  private double heading;
  private Instant deviceTs;
  private Instant processedAt;

 
  @JsonGetter("requestId")
  public String getRequestIdSafe() {
    return requestId != null ? requestId : RequestContext.getTraceId();
  }

  @JsonGetter("processedAt")
  public Instant getProcessedAtSafe() {
    return processedAt != null ? processedAt : Instant.now();
  }

}
