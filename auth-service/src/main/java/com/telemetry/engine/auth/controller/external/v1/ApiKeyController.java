package com.telemetry.engine.auth.controller.external.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.auth.core.apikey.dto.request.ApiKeyCreateRequest;
import com.telemetry.engine.auth.core.apikey.dto.response.ApiKeyCreateResponse;
import com.telemetry.engine.auth.core.apikey.service.ApiKeyService;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import jakarta.validation.Valid;

@RequestMapping(path = "/api/external/v1/api-keys")
@RestController
public class ApiKeyController {
  @Autowired
  private ApiKeyService apiKeyService;

  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<ApiKeyCreateResponse>> createApiKey(
      @RequestBody @Valid ServiceRequest<ApiKeyCreateRequest> serviceRequest) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(apiKeyService.createApiKey(serviceRequest));
  }


  @PostMapping(path = "/{id}/revoke", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<Void>> revokeApiKey(@PathVariable Long id) {

    return ResponseEntity.ok(apiKeyService.revokeApiKey(id));
  }

  @DeleteMapping(path = "/{id}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<Void>> deleteApiKey(@PathVariable Long id) {

    return ResponseEntity.ok(apiKeyService.deleteApiKey(id));
  }

}
