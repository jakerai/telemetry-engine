package com.telemetry.engine.query.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NearbyAssetsRequest {

  private String assetType;
  private double lat;
  private double lon;
  private int radiusMeters;
  
}
