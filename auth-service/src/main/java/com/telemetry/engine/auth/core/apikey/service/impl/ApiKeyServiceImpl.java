package com.telemetry.engine.auth.core.apikey.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyCreateRequest;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyValidationRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyCreateResponse;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyValidationResponse;
import com.telemetry.engine.auth.core.apikey.entity.ApiKey;
import com.telemetry.engine.auth.core.apikey.mapper.ApiKeyMapper;
import com.telemetry.engine.auth.core.apikey.persistence.ApiKeyPersistence;
import com.telemetry.engine.auth.core.apikey.service.ApiKeyService;
import com.telemetry.engine.auth.security.apikey.ApiKeyGenerator;
import com.telemetry.engine.auth.security.model.AuthenticatedUser;
import com.telemetry.engine.auth.util.AuthUtil;
import com.telemetry.engine.common.context.RequestContext;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.exception.NotFoundException;
import com.telemetry.engine.common.utils.SecretUtils;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {
  private static final Logger log = LoggerFactory.getLogger(ApiKeyServiceImpl.class);

  private final ApiKeyPersistence apiKeyPersistence;


  @Override
  public ServiceResponse<ApiKeyCreateResponse> createApiKey(
      ServiceRequest<ApiKeyCreateRequest> serviceRequest) {

    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    ApiKeyCreateRequest apiKeyCreateRequest = serviceRequest.payload();

    log.info("Creating API key from ip={} for asset ID={} by user ID={}", clientIp,
        apiKeyCreateRequest.getAssetId(), currentUser.getId());

    String generatedApiKey = ApiKeyGenerator.generate();

    ApiKey apiKey = ApiKeyMapper.toEntity(apiKeyCreateRequest);
    apiKey.setKey(SecretUtils.hash(generatedApiKey));
    apiKey.setUserId(currentUser.getId());

    apiKey = apiKeyPersistence.save(apiKey);

    ApiKeyCreateResponse data =
        ApiKeyCreateResponse.from(generatedApiKey, apiKey.getExpiresAt(), apiKey.isActive());
    return ServiceResponse.success(data, "API key created successfully");
  }


  @Override
  public ServiceResponse<ApiKeyValidationResponse> validateApiKey(
      ServiceRequest<ApiKeyValidationRequest> serviceRequest) {
    String clientIp = RequestContext.getClientIp();

    ApiKeyValidationRequest apiKeyValidationRequest = serviceRequest.payload();
    String rawApiKey = apiKeyValidationRequest.getApiKey();

    log.info("Validating API key from ip={}", clientIp);
    String hashedKey = SecretUtils.hash(rawApiKey);
    
    ApiKey apiKey = apiKeyPersistence.findByKeyAndActiveTrueAndRevokedFalseAndDeletedFalse(hashedKey)
        .orElseThrow(() -> new NotFoundException("API key not found or is invalid"));
    
    ApiKeyValidationResponse data = ApiKeyValidationResponse.from(apiKey);
    return ServiceResponse.success(data, "API key validated successfully");
  }



  @Override
  public ServiceResponse<Void> revokeApiKey(Long id) {
    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();

    log.info("Revoking API key with ID={} for user ID from ip={}", id, currentUser.getId(),
        clientIp);


    ApiKey apiKey = apiKeyPersistence.findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(id)
        .orElseThrow(() -> new NotFoundException("API key not found or already revoked: id=" + id));

    apiKey.setRevoked(true);
    apiKey.setActive(false);
    apiKeyPersistence.save(apiKey);
    return ServiceResponse.success("API key revoked successfully");
  }


  @Override
  public ServiceResponse<Void> deleteApiKey(Long id) {
    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();

    log.info("Revoking API key with ID={} for user ID from ip={}", id, currentUser.getId(),
        clientIp);
    ApiKey apiKey = apiKeyPersistence.findByIdAndActiveTrueAndRevokedFalseAndDeletedFalse(id)
        .orElseThrow(() -> new NotFoundException("API key not found or already revoked: id=" + id));

    apiKey.setActive(false);
    apiKey.setDeleted(true);
    apiKeyPersistence.save(apiKey);
    return ServiceResponse.success("API key deleted successfully");
  }

}
