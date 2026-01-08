package com.telemetry.engine.auth.controller.external.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.auth.core.identity.dto.request.ForgotPasswordRequest;
import com.telemetry.engine.auth.core.identity.dto.request.LoginRequest;
import com.telemetry.engine.auth.core.identity.dto.request.ResetPasswordRequest;
import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.dto.response.RefreshTokenResponse;
import com.telemetry.engine.auth.core.identity.service.IdentityService;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import jakarta.validation.Valid;

@RequestMapping(path = "/api/external/v1/auth")
@RestController
public class AuthController {

  @Autowired
  private IdentityService identityService;

  @PostMapping(path = "/signup", consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<?>> signup(
      @RequestBody @Valid ServiceRequest<SignupRequest> serviceRequest) {

    return ResponseEntity.ok(identityService.signup(serviceRequest));
  }

  @PostMapping(path = "/login", consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<LoginResponse>> login(
      @RequestBody @Valid ServiceRequest<LoginRequest> serviceRequest) {

    return ResponseEntity.ok(identityService.login(serviceRequest));
  }

  @PostMapping(path = "/logout", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<?>> logout() {

    return ResponseEntity.ok(identityService.logout());
  }

  @PostMapping(path = "/refresh-token", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<RefreshTokenResponse>> refreshToken() {

    return ResponseEntity.ok(identityService.refreshToken());
  }

  @PostMapping(path = "/forgot-password", consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<?>> forgotPassword(
      @RequestBody @Valid ServiceRequest<ForgotPasswordRequest> serviceRequest) {

    return ResponseEntity.ok(identityService.forgotPassword(serviceRequest));
  }


  @PostMapping("/reset-password")
  public ResponseEntity<ServiceResponse<?>> resetPassword(
      @RequestBody @Valid ServiceRequest<ResetPasswordRequest> serviceRequest) {

    return ResponseEntity.ok(identityService.resetPassword(serviceRequest));
  }
 
}
