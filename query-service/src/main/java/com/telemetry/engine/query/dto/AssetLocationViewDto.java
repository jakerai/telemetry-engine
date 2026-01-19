package com.telemetry.engine.query.dto;

import java.time.Instant;
import com.telemetry.engine.query.enums.AssetStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AssetLocationViewDto {
  
  // Asset info
  private Long assetId;
  private String name;
  private String model;
  private Long typeId;
  private String typeCode;
  private AssetStatus status;
  private Long ownerId;
  private Long operatorId;

  // Current location info
  private Double currentLat;
  private Double currentLon;
  private Double speed;
  private Double heading;
  private Instant deviceTs;
  
}
