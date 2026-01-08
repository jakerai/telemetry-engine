package com.telemetry.engine.auth.core.asset.entity;

import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import com.telemetry.engine.auth.core.asset.enums.AssetCategory;
import com.telemetry.engine.auth.core.asset.enums.AssetStatus;
import com.telemetry.engine.auth.core.asset.enums.AssetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "asset",
    indexes = {@Index(name = "idx_asset_id", columnList = "asset_id"),
        @Index(name = "idx_asset_owner", columnList = "owner_id"),
        @Index(name = "idx_asset_category_type", columnList = "category,type"),
        @Index(name = "idx_asset_status", columnList = "status")})
public class Asset extends BaseEntity {

  @Column(name = "asset_id", nullable = false, unique = true, length = 255)
  private String assetId;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "model", nullable = false, length = 255)
  private String model;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 50)
  private AssetCategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 50)
  private AssetType type;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private AssetStatus status = AssetStatus.ACTIVE;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  public boolean isActive() {
    return status == AssetStatus.ACTIVE;
  }

  public boolean isDeleted() {
    return status == AssetStatus.DELETED;
  }

}
