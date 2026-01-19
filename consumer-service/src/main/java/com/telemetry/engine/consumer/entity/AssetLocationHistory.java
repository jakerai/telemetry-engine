package com.telemetry.engine.consumer.entity;

import java.time.Instant;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import io.r2dbc.postgresql.codec.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "telemetry.asset_location_history")
public class AssetLocationHistory {

  @Column("device_ts")
  private Instant deviceTs; // time-series partition key
  
  private Long assetId;
  
  private Double latitude;
  private Double longitude;

  @Column("location") 
  private Point location; 
  
  @Column("h3_index")
  private Long h3Index;

  private Double speed;
  private Double heading;
  
  private Instant processedAt;
}
 