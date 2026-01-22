package com.telemetry.engine.auth.core.apikey.dto.response;

import java.time.Instant;
import com.telemetry.engine.auth.core.apikey.dto.ApiKeyDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ApiKeyCreateResponse extends ApiKeyDto {
  
  public static ApiKeyCreateResponse from(String generatedApiKey, Instant expiresAt, boolean active) {
    return ApiKeyCreateResponse.builder().key(generatedApiKey).expiresAt(expiresAt)
        .active(active).build();
  }

}
