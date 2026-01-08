package com.telemetry.engine.gateway.security.apikey.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiKeyMeta {
  private String apiKeyHash;
  private Long userId;
  private String assetId;
  private Instant expiresAt;
  private boolean valid;
}
