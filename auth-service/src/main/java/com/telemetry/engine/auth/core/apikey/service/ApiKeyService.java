package com.telemetry.engine.auth.core.apikey.service;

import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyCreateRequest;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyValidationRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyCreateResponse;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyValidationResponse;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;

public interface ApiKeyService {

  ServiceResponse<ApiKeyCreateResponse> createApiKey(ServiceRequest<ApiKeyCreateRequest> serviceRequest);

  ServiceResponse<ApiKeyValidationResponse> validateApiKey(ServiceRequest<ApiKeyValidationRequest> serviceRequest);

  ServiceResponse<Void> revokeApiKey(Long id);

  ServiceResponse<Void> deleteApiKey(Long id);

}
