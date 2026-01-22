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
  private Long typeId;
  private AssetStatus status;
  private Long ownerId;
  private Long operatorId;

  public static Asset from(AssetCreateRequest createAsset) {
    return Asset.builder().name(createAsset.getName()).model(createAsset.getModel())
        .typeId(createAsset.getTypeId()).operatorId(createAsset.getOperatorId())
        .ownerId(createAsset.getOwnerId()).build();
  }

}
