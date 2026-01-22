package com.telemetry.engine.auth.controller.internal.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyValidationRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyValidationResponse;
import com.telemetry.engine.auth.core.apikey.service.ApiKeyService;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import jakarta.validation.Valid;


@RequestMapping(path = "/api/internal/v1/api-keys")
@RestController
public class ApiKeyValidationController {

  @Autowired
  private ApiKeyService apiKeyService;

  @PostMapping(path = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<ApiKeyValidationResponse>> validateApiKey(
      @RequestBody @Valid ServiceRequest<ApiKeyValidationRequest> serviceRequest) {

    return ResponseEntity.ok(apiKeyService.validateApiKey(serviceRequest));
  }

}
