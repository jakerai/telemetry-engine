package com.telemetry.engine.query.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackingRequest {

  private Long assetId;
  
}
