package com.telemetry.engine.query.dto.resquest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NearbyAssetsRequest {

  private Long assetTypeId;
  private double lat;
  private double lon;
  private int radiusMeters;

}
