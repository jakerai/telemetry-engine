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

  @Column("asset_id")
  private Long assetId;
  
  @Column("device_ts")
  private Instant deviceTs; // time-series partition key
  
  @Column("operator_id")
  private Long operatorId;
  
  @Column("lon")
  private Double longitude;
    
  @Column("lat")
  private Double latitude;

  @Column("location") 
  private Point location;
  
  @Column("h3_index")
  private Long h3Index;

  @Column("speed")
  private Double speed;
  
  @Column("heading")
  private Double heading;
    
  @Column("processed_at")
  private Instant processedAt;
}
 