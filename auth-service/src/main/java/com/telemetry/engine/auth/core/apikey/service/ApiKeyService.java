package com.telemetry.engine.auth.core.apikey.service;

import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyRegistrationRequest;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyValidationRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyRegistrationResponse;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyValidationResponse;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;

public interface ApiKeyService {

  ServiceResponse<ApiKeyRegistrationResponse> createApiKey(
      ServiceRequest<ApiKeyRegistrationRequest> serviceRequest);

  ServiceResponse<ApiKeyValidationResponse> validateApiKey(
      ServiceRequest<ApiKeyValidationRequest> serviceRequest);

  ServiceResponse<?> revokeApiKey(Long id);

  ServiceResponse<?> deleteApiKey(Long id);

}
