package com.telemetry.engine.common.redis.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetRedisState {
  Long assetId;
  Long assetTypeId;
  Long operatorId;
  
  double latitude;
  double longitude;

  String h3Index;

  double speed;
  double heading;

  Instant deviceTs;  
  Instant processedAt;
}
