package com.telemetry.engine.query.dto.resquest;

import com.telemetry.engine.query.entity.Asset;
import com.telemetry.engine.query.enums.AssetStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetCreateRequest {

  private String name;
  private String model;
  private Long assetTypeId;
  private Long ownerId;
  private String assetCode;
  private String serialNumber;

  public static Asset from(AssetCreateRequest createAsset) {
    return Asset.builder().name(createAsset.getName()).model(createAsset.getModel())
        .assetCode(createAsset.getAssetCode()).serialNumber(createAsset.getSerialNumber())
        .status(AssetStatus.ACTIVE).typeId(createAsset.getAssetTypeId())
        .ownerId(createAsset.getOwnerId()).build();
  }

}
