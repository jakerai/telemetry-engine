package com.telemetry.engine.query.entity;

import java.time.Instant;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import io.r2dbc.postgresql.codec.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "telemetry.asset_location_history")
public class AssetLocationHistory {

  @Column("device_ts")
  private Instant deviceTs; // time-series partition key
  
  @Column("asset_id")
  private Long assetId;
  
  @Column("lat")
  private Double latitude;

  @Column("lon")
  private Double longitude;

  @Column("location") 
  private Point location;
  
  @Column("h3_index")
  private Long h3Index;

  @Column("speed")
  private Double speed;
  
  @Column("heading")
  private Double heading;
  
  @Column("operator_id")
  private Long operatorId;
  
  @Column("processed_at")
  private Instant processedAt;
}
 