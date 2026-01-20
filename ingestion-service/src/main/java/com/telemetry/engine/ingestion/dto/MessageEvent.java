package com.telemetry.engine.ingestion.dto;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.telemetry.engine.common.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
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

  // No-args constructor for Jackson
  public MessageEvent() {
    // Ensure defaults even if deserialized
    if (this.requestId == null)
      this.requestId = RequestContext.getTraceId();
    if (this.processedAt == null)
      this.processedAt = Instant.now();
  }

  // Custom setter for Jackson deserialization
  @JsonProperty("requestId")
  public void setRequestIdSafe(String requestId) {
    this.requestId = (requestId != null) ? requestId : RequestContext.getTraceId();
  }

  @JsonProperty("processedAt")
  public void setProcessedAtSafe(Instant processedAt) {
    this.processedAt = (processedAt != null) ? processedAt : Instant.now();
  }

}
