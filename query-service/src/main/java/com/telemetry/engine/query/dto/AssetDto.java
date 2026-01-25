package com.telemetry.engine.query.dto;


import java.time.Instant;
import com.telemetry.engine.query.entity.Asset;
import com.telemetry.engine.query.enums.AssetStatus;
import lombok.Builder;

@Builder
public record AssetDto(Long assetId, String name, String model, String serialNumber, Long typeId,
    String assetType, String category, AssetStatus status, Long ownerId, Long createdBy,
    Instant createdAt, Long modifiedBy, Instant modifiedAt) {

  public static AssetDto from(Asset asset) {
    return AssetDto.builder().assetId(asset.getId()).name(asset.getName()).model(asset.getModel())
        .serialNumber(asset.getSerialNumber()).typeId(asset.getTypeId()).status(asset.getStatus())
        .ownerId(asset.getOwnerId()).createdBy(asset.getCreatedBy()).createdAt(asset.getCreatedAt())
        .modifiedBy(asset.getModifiedBy()).modifiedAt(asset.getModifiedAt()).build();
  }
}
