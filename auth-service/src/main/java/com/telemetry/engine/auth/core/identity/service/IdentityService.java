package com.telemetry.engine.auth.core.identity.service;

import com.telemetry.engine.auth.core.identity.dto.request.ForgotPasswordRequest;
import com.telemetry.engine.auth.core.identity.dto.request.LoginRequest;
import com.telemetry.engine.auth.core.identity.dto.request.ResetPasswordRequest;
import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.dto.response.RefreshTokenResponse;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;

public interface IdentityService {

  // User
  ServiceResponse<?> signup(ServiceRequest<SignupRequest> serviceRequest);

  ServiceResponse<LoginResponse> login(ServiceRequest<LoginRequest> serviceRequest);

  ServiceResponse<?> logout();

  ServiceResponse<RefreshTokenResponse> refreshToken();

  ServiceResponse<?> forgotPassword(ServiceRequest<ForgotPasswordRequest> serviceRequest);

  ServiceResponse<?> resetPassword(ServiceRequest<ResetPasswordRequest> serviceRequest);

}
