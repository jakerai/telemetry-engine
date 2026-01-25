package com.telemetry.engine.query.dto.response;

import com.telemetry.engine.query.entity.Asset;
import com.telemetry.engine.query.enums.AssetStatus;
import lombok.Builder;

@Builder
public record AssetUpdateResponse(
    Long assetId,
    String name,
    String model,
    String serialNumber,
    Long typeId,
    AssetStatus status,
    Long ownerId
) {

  public static AssetUpdateResponse from(Asset asset) {
    return AssetUpdateResponse.builder()
        .assetId(asset.getId())
        .name(asset.getName())
        .model(asset.getModel())
        .serialNumber(asset.getSerialNumber())
        .typeId(asset.getTypeId())
        .status(asset.getStatus())
        .ownerId(asset.getOwnerId())
        .build();
  }
}
