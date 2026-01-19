package com.telemetry.engine.query.entity;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import io.r2dbc.postgresql.codec.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


/**
 * This table stores only the latest location per asset for ultra-fast map queries.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("telemetry.asset_current_location")
public class AssetCurrentLocation {

  @Id
  @Column("asset_id")
  private Long assetId;

  @Column("current_lat")
  private Double currentLat;

  @Column("current_lon")
  private Double currentLon;

  @Column("location")
  private Point location;

  // For H3
  @Column("h3_index")
  private Long h3Index;

  @Column("speed")
  private Double speed;

  @Column("heading")
  private Double heading;

  @Column("device_ts")
  private Instant deviceTs;

  @Column("processed_at")
  private Instant processedAt;

  @Column("created_by")
  private Long createdBy;

  @Column("modified_by")
  private Long modifiedBy;

  @Column("created_at")
  @CreatedDate
  private Instant createdAt;

  @Column("modified_at")
  @LastModifiedDate
  private Instant modifiedAt;
}
