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
public class AssetUpdateRequest {

  @NotNull(message = "Asset ID is required")
  @Min(value = 1, message = "Asset ID must be greater than 0")
  private Long assetId;

  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters long")
  private String name;

  @NotBlank(message = "Model is required")
  @Size(min = 2, max = 100, message = "Model must be between 2 and 100 characters long")
  private String model;

  @NotBlank(message = "Serial number is required")
  @Size(min = 2, max = 100, message = "Serial number must be between 2 and 100 characters long")
  @Pattern(regexp = "^[A-Za-z0-9-_]+$",
      message = "Serial number can contain only letters, numbers, hyphen and underscore")
  private String serialNumber;

  @NotNull(message = "AssetTypeId is required")
  @Min(value = 1, message = "AssetTypeId must be greater than 0")
  private Long assetTypeId;

  @NotNull(message = "Status is required")
  private AssetStatus status;

  @NotNull(message = "OwnerId is required")
  @Min(value = 1, message = "OwnerId must be greater than 0")
  private Long ownerId;


  public static boolean applyIfChanged(Asset existing, AssetUpdateRequest req) {
    boolean changed = false;

    if (req.getName() != null && !req.getName().equals(existing.getName())) {
      existing.setName(req.getName());
      changed = true;
    }

    if (req.getModel() != null && !req.getModel().equals(existing.getModel())) {
      existing.setModel(req.getModel());
      changed = true;
    }

    if (req.getSerialNumber() != null
        && !req.getSerialNumber().equals(existing.getSerialNumber())) {
      existing.setSerialNumber(req.getSerialNumber());
      changed = true;
    }

    if (req.getAssetTypeId() != null && !req.getAssetTypeId().equals(existing.getTypeId())) {
      existing.setTypeId(req.getAssetTypeId());
      changed = true;
    }

    if (req.getStatus() != null && req.getStatus() != existing.getStatus()) {
      existing.setStatus(req.getStatus());
      changed = true;
    }

    if (req.getOwnerId() != null && !req.getOwnerId().equals(existing.getOwnerId())) {
      existing.setOwnerId(req.getOwnerId());
      changed = true;
    }

    return changed;
  }


}
