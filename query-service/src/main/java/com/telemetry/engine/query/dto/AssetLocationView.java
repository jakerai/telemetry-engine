package com.telemetry.engine.query.dto;

import java.time.Instant;

public class AssetLocationView {
  
  private String name;
  private String model;
  private String assetCode;
  private String serialNumber;
  private String status;
  private Long ownerId;
  private String category;
  private String type;
  private Double latitude;
  private Double longitude;
  private Double heading;
  private Double speed;
  private Long operatorId;
  private Instant deviceTs;
}
