package com.telemetry.engine.query.dto.resquest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetFilter {
  private String createdBy;
  private String modifiedBy;
  private String category;
  private Long typeId;
  private Long ownerId;
  private String sortBy;
  private String sortDir; // DESC or ASC
}
