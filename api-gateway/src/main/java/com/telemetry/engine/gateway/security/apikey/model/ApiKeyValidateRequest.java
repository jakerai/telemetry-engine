package com.telemetry.engine.gateway.security.apikey.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApiKeyValidateRequest {

  private String apiKey;
  
}
