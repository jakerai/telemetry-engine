package com.telemetry.engine.query.dto.resquest;

import com.telemetry.engine.query.entity.Asset;
import com.telemetry.engine.query.enums.AssetStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetCreateRequest {
  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters long")
  private String name;

  @NotBlank(message = "Model is required")
  @Size(min = 2, max = 100, message = "Model must be between 2 and 100 characters long")
  private String model;

  @NotNull(message = "AssetTypeId is required")
  @Min(value = 1, message = "AssetTypeId must be greater than 0")
  private Long assetTypeId;

  @NotNull(message = "OwnerId is required")
  @Min(value = 1, message = "OwnerId must be greater than 0")
  private Long ownerId;

  @NotBlank(message = "Serial number is required")
  @Size(min = 2, max = 100, message = "Serial number must be between 2 and 100 characters long")
  @Pattern(
      regexp = "^[A-Za-z0-9-_]+$",
      message = "Serial number can contain only letters, numbers, hyphen and underscore"
  )
  private String serialNumber;

  public static Asset from(AssetCreateRequest createAsset) {
    return Asset.builder().name(createAsset.getName()).model(createAsset.getModel())
        .serialNumber(createAsset.getSerialNumber()).status(AssetStatus.ACTIVE)
        .typeId(createAsset.getAssetTypeId()).ownerId(createAsset.getOwnerId()).build();
  }

}
