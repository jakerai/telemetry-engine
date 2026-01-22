package com.telemetry.engine.auth.core.apikey.mapper;

import java.time.Duration;
import java.time.Instant;
import com.telemetry.engine.auth.core.apikey.dto.ApiKeyDto;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyCreateRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyCreateResponse;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;

public class ApiKeyMapper {

  public static ApiKeyCreateResponse toApiKeyRegistrationResponse(String key,
      Instant expiresAt, boolean active) {
    return ApiKeyCreateResponse.builder().key(key).expiresAt(expiresAt).active(active)
        .build();
  }


  public static ApiKeyDto toApiKeyDto(ApiKey apiKey) {
    if (apiKey == null)
      return null;

    return ApiKeyDto.builder().id(apiKey.getId()).userId(apiKey.getUserId()).key(apiKey.getKey())
        .expiresAt(apiKey.getExpiresAt()).active(apiKey.isActive()).createdAt(apiKey.getCreatedAt())
        .modifiedAt(apiKey.getModifiedAt()).build();
  }


  public static ApiKey toEntity(ApiKeyCreateRequest payload) {
    return ApiKey.builder()
        .expiresAt(Instant.now().plus(Duration.ofDays(payload.getExpiresInDays()))).build();
  }

}
