package com.telemetry.engine.auth.core.identity.service;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.telemetry.engine.auth.core.identity.dto.request.LoginRequest;
import com.telemetry.engine.auth.core.identity.dto.request.MobileOauth2LoginRequest;
import com.telemetry.engine.auth.core.identity.dto.request.PasswordForgotRequest;
import com.telemetry.engine.auth.core.identity.dto.request.PasswordResetRequest;
import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.dto.response.RefreshTokenResponse;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;

public interface IdentityService {

  ServiceResponse<Void> signup(ServiceRequest<SignupRequest> serviceRequest);

  ServiceResponse<LoginResponse> login(ServiceRequest<LoginRequest> serviceRequest);

  ServiceResponse<LoginResponse> loginOauth2Web(OAuth2User oauth2User,
      OAuth2AuthenticationToken authentication);

  ServiceResponse<Void> logout();

  ServiceResponse<RefreshTokenResponse> refreshToken();

  ServiceResponse<Void> forgotPassword(ServiceRequest<PasswordForgotRequest> serviceRequest);

  ServiceResponse<Void> resetPassword(ServiceRequest<PasswordResetRequest> serviceRequest);

  ServiceResponse<LoginResponse> loginOauth2Mobile(
      ServiceRequest<MobileOauth2LoginRequest> serviceRequest);

}
