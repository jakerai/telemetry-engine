package com.telemetry.engine.consumer.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
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
@Table("asset_event")
public class AssetEvent {

  @Id
  private Long id; 

  private String assetId; 
  private double latitude;
  private double longitude;
  private double speed;
  private double heading;
  private Instant processedAt;

}
