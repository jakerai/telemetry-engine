package com.telemetry.engine.auth.core.apikey.mapper;

import java.time.Duration;
import java.time.Instant;
import com.telemetry.engine.auth.core.apikey.dto.ApiKeyDto;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyRegistrationRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyRegistrationResponse;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;

public class ApiKeyMapper {

  public static ApiKeyRegistrationResponse toApiKeyRegistrationResponse(String key,
      Instant expiresAt, boolean active) {
    return ApiKeyRegistrationResponse.builder().key(key).expiresAt(expiresAt).active(active)
        .build();
  }


  public static ApiKeyDto toApiKeyDto(ApiKey apiKey) {
    if (apiKey == null)
      return null;

    return ApiKeyDto.builder().id(apiKey.getId()).userId(apiKey.getUserId()).key(apiKey.getKey())
        .expiresAt(apiKey.getExpiresAt()).active(apiKey.isActive()).createdAt(apiKey.getCreatedAt())
        .modifiedAt(apiKey.getModifiedAt()).build();
  }


  public static ApiKey toEntity(ApiKeyRegistrationRequest payload) {
    return ApiKey.builder()
        .expiresAt(Instant.now().plus(Duration.ofDays(payload.getExpiresInDays()))).build();
  }

}
