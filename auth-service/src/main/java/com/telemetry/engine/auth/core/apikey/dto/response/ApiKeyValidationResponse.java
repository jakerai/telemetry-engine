package com.telemetry.engine.auth.core.apikey.dto.response;

import java.time.Instant;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyValidationResponse {
  private boolean valid;
  private String apiKeyHash;
  private Long userId;
  private Instant expiresAt;

  public static ApiKeyValidationResponse from(ApiKey apiKey) {
    return ApiKeyValidationResponse.builder().valid(apiKey.isValid()).apiKeyHash(apiKey.getKey())
        .userId(apiKey.getUserId()).expiresAt(apiKey.getExpiresAt()).build();
  }


}
