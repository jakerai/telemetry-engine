package com.telemetry.engine.consumer.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageEvent {

  private Long assetId;
  private Long assetTypeId;
  private Long operatorId;
  private double latitude;
  private double longitude;
  private double speed;
  private double heading;
  private Instant deviceTs;
  private Instant processedAt;
  private String requestId;

}
