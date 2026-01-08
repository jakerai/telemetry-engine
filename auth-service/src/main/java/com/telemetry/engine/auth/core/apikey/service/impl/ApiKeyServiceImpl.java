package com.telemetry.engine.auth.core.apikey.service.impl;

import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyRegistrationRequest;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyValidationRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyRegistrationResponse;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyValidationResponse;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;
import com.telemetry.engine.auth.core.apikey.mapper.ApiKeyMapper;
import com.telemetry.engine.auth.core.apikey.persistence.ApiKeyPersistence;
import com.telemetry.engine.auth.core.apikey.service.ApiKeyService;
import com.telemetry.engine.auth.core.identity.util.AuthUtil;
import com.telemetry.engine.auth.security.AuthenticatedUser;
import com.telemetry.engine.auth.security.apikey.ApiKeyGenerator;
import com.telemetry.engine.common.context.RequestContext;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.exception.NotFoundException;
import com.telemetry.engine.common.utils.ResponseBuilder;
import com.telemetry.engine.common.utils.SecretUtils;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {
  private static final Logger log = LoggerFactory.getLogger(ApiKeyServiceImpl.class);

  private final ApiKeyPersistence apiKeyPersistence;


  @Override
  public ServiceResponse<ApiKeyRegistrationResponse> createApiKey(
      ServiceRequest<ApiKeyRegistrationRequest> serviceRequest) {

    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    ApiKeyRegistrationRequest payload = serviceRequest.getPayload();
    log.info(
        "[AuthServiceImpl.createApiKey] Creating API key from ip={} for asset ID={} by user ID={}",
        clientIp, payload.getAssetId(), currentUser.getId());

    String generatedApiKey = ApiKeyGenerator.generate();

    ApiKey apiKey = ApiKeyMapper.toEntity(payload);
    apiKey.setKey(SecretUtils.hash(generatedApiKey));
    apiKey.setUserId(currentUser.getId());

    apiKey = apiKeyPersistence.save(apiKey);

    ApiKeyRegistrationResponse responsePayload = ApiKeyMapper
        .toApiKeyRegistrationResponse(generatedApiKey, apiKey.getExpiresAt(), apiKey.isActive());

    return ResponseBuilder.successWithPayload("API Key created successfully", responsePayload);
  }


  @Override
  public ServiceResponse<ApiKeyValidationResponse> validateApiKey(
      ServiceRequest<ApiKeyValidationRequest> serviceRequest) {
    String clientIp = RequestContext.getClientIp();

    String rawApiKey = serviceRequest.getPayload().getApiKey();

    log.info("[AuthServiceImpl.validateApiKey] Validating API key from ip={}", clientIp);
    String hashedKey = SecretUtils.hash(rawApiKey);
    Optional<ApiKey> apiKeyOpt =
        apiKeyPersistence.findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(hashedKey);

    boolean isValid = isKeyValid(apiKeyOpt);

    ApiKey apiKey = apiKeyOpt.get();


    ApiKeyValidationResponse responsePayload = ApiKeyValidationResponse.builder().valid(isValid)
        .apiKeyHash(hashedKey).userId(apiKey.getId()).expiresAt(apiKey.getExpiresAt()).build();

    return ResponseBuilder.successWithPayload("API Key validation completed", responsePayload);
  }

  private boolean isKeyValid(Optional<ApiKey> apiKeyOpt) {
    return apiKeyOpt.filter(key -> key.isActive() && !key.isRevoked() && !key.isDeleted())
        .filter(this::isNotExpired).isPresent();
  }

  private boolean isNotExpired(ApiKey key) {
    return key.getExpiresAt() == null || key.getExpiresAt().isAfter(Instant.now());
  }


  @Override
  public ServiceResponse<?> revokeApiKey(Long id) {
    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();

    log.info("[AuthServiceImpl.revokeApiKey] Revoking API key with ID={} for user ID from ip={}",
        id, currentUser.getId(), clientIp);


    ApiKey apiKey = apiKeyPersistence.findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(id)
        .orElseThrow(() -> new NotFoundException("API key not found or already revoked: id=" + id));

    apiKey.setRevoked(true);
    apiKey.setActive(false);
    apiKeyPersistence.save(apiKey);

    return ResponseBuilder.success("API Key revoked successfully");
  }


  @Override
  public ServiceResponse<?> deleteApiKey(Long id) {
    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();

    log.info("[AuthServiceImpl.revokeApiKey] Revoking API key with ID={} for user ID from ip={}",
        id, currentUser.getId(), clientIp);
    ApiKey apiKey = apiKeyPersistence.findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(id)
        .orElseThrow(() -> new NotFoundException("API key not found or already revoked: id=" + id));

    apiKey.setActive(false);
    apiKey.setDeleted(true);
    apiKeyPersistence.save(apiKey);

    return ResponseBuilder.success("API Key deleted successfully");
  }

}
