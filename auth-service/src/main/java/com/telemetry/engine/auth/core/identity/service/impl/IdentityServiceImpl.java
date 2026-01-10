package com.telemetry.engine.auth.core.identity.service.impl;

import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.telemetry.engine.auth.core.activity.enums.Action;
import com.telemetry.engine.auth.core.activity.service.ActivityService;
import com.telemetry.engine.auth.core.identity.dto.request.ForgotPasswordRequest;
import com.telemetry.engine.auth.core.identity.dto.request.LoginRequest;
import com.telemetry.engine.auth.core.identity.dto.request.ResetPasswordRequest;
import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.dto.response.RefreshTokenResponse;
import com.telemetry.engine.auth.core.identity.service.IdentityService;
import com.telemetry.engine.auth.core.token.dto.TokenDto;
import com.telemetry.engine.auth.core.token.service.TokenService;
import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.user.mapper.UserMapper;
import com.telemetry.engine.auth.core.user.service.UserService;
import com.telemetry.engine.auth.core.verification.enums.VerificationChannel;
import com.telemetry.engine.auth.core.verification.enums.VerificationIntent;
import com.telemetry.engine.auth.core.verification.service.VerificationService;
import com.telemetry.engine.auth.security.jwt.enums.TokenType;
import com.telemetry.engine.auth.security.jwt.service.JwtService;
import com.telemetry.engine.auth.security.model.AuthenticatedUser;
import com.telemetry.engine.auth.security.model.JwtToken;
import com.telemetry.engine.auth.util.AuthUtil;
import com.telemetry.engine.common.context.RequestContext;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.exception.InvalidTokenException;
import com.telemetry.engine.common.exception.UnauthorizedException;
import com.telemetry.engine.common.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityServiceImpl implements IdentityService {
  private static final Logger log = LoggerFactory.getLogger(IdentityServiceImpl.class);

  private final AuthenticationConfiguration authenticationConfiguration;

  private final JwtService jwtService;
  private final UserService userService;
  private final ActivityService activityService;
  private final TokenService tokenService;
  private final VerificationService verificationService;



  private AuthenticatedUser authenticateUser(String username, String password) {
    log.info("Authenticating user with username={}", username);
    try {
      AuthenticationManager authenticationManager =
          authenticationConfiguration.getAuthenticationManager();

      Authentication authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(username, password));

      return (AuthenticatedUser) authentication.getPrincipal();

    } catch (BadCredentialsException ex) {
      log.warn("Authentication failed: invalid credentials for username={}", username, ex);
      throw new UnauthorizedException("Invalid username or password");

    } catch (AuthenticationException ex) {
      log.error("Authentication failed for username={} - {}", username, ex.getMessage(), ex);
      throw new UnauthorizedException(ex.getMessage());

    } catch (Exception ex) {
      log.error("Unable to obtain AuthenticationManager", ex);
      throw new IllegalStateException("Authentication system error");
    }
  }


  @Transactional
  @Override
  public ServiceResponse<?> signup(ServiceRequest<SignupRequest> serviceRequest) {
    String clientIp = RequestContext.getClientIp();

    SignupRequest signupDto = serviceRequest.getPayload();

    log.info("New user signup for email={} from ip={}",
        signupDto.getEmail(), clientIp);

    UserDto userDto = userService.createUserOrThrow(signupDto);

    log.info("New User signed up successfully with ID={}, email={}", userDto.getId(),
        userDto.getEmail());

    verificationService.sendVerification(userDto, VerificationIntent.SIGNUP,
        VerificationChannel.EMAIL);

    return ResponseBuilder.success("Signup successfully");
  }


  @Override
  public ServiceResponse<LoginResponse> login(ServiceRequest<LoginRequest> serviceRequest) {

    LoginRequest loginRequest = serviceRequest.getPayload();
    String clientIp = RequestContext.getClientIp();

    log.info("Login request for email={} from ip={}",
        loginRequest.getEmail(), clientIp);

    AuthenticatedUser user = authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
    UserDto userDto = userService.updateLoginMetadataOrThrow(user.getId(), clientIp, Instant.now());

    activityService.logActivity(Action.LOGIN_SUCCESS, userDto.getId(), clientIp);

    JwtToken generatedRefreshToken =
        jwtService.generateRefreshTokenOrThrow(userDto.getUsername(), userDto.getId());

    TokenDto refreshToken =
        tokenService.storeTokenOrThrow(userDto.getId(), generatedRefreshToken.getValue(),
            TokenType.REFRESH, generatedRefreshToken.getExpirationTime());

    JwtToken generatedAccessToken =
        jwtService.generateAccessTokenOrThrow(userDto.getUsername(), userDto.getId(),
            userDto.getRolesAsList(), userDto.getPermissionsAsList(), refreshToken.getId());

    LoginResponse loginResponse = UserMapper.toUserLoginResponse(generatedAccessToken.getValue(),
        generatedAccessToken.getExpiresIn(), generatedRefreshToken.getValue(), userDto);
    log.info("Login successful for email: {}", userDto.getEmail());

    return ResponseBuilder.successWithPayload("Login successful", loginResponse);
  }

  @Override
  public ServiceResponse<?> logout() {

    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    String clientIp = RequestContext.getClientIp();
    Long refreshTokenId = RequestContext.getRefreshTokenId();
    log.info("Logout request for user ID={} from ip={}",
        currentUser.getId(), clientIp);

    tokenService.revokeTokenById(refreshTokenId);
    activityService.logActivity(Action.LOGOUT, currentUser.getId(), clientIp);

    log.info("Logout successful for userId={}", currentUser.getId());
    return ResponseBuilder.success("User logged out successfully");

  }

  @Override
  public ServiceResponse<RefreshTokenResponse> refreshToken() {

    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    log.info("New refresh token request by user ID= {} from ip={}",
        currentUser.getId(), clientIp);
    Jwt jwt = currentUser.getCurrentJwt();

    String oldRefreshTokenValue = jwt.getTokenValue();

    // Validate token format/expiration
    if (!jwtService.validateRefreshToken(oldRefreshTokenValue)) {
      throw new InvalidTokenException("Refresh token expired or invalid token");
    }

    // Validate token exists in DB and is active
    tokenService.validateTokenOrThrow(oldRefreshTokenValue);

    // Fetch active user
    UserDto userDto = userService.getUserOrThrow(currentUser.getId());

    // Generate new refresh token (throws TokenGenerationException internally if fails)
    JwtToken generatedRefreshToken =
        jwtService.generateRefreshTokenOrThrow(userDto.getUsername(), userDto.getId());

    // Revoke old token in DB
    tokenService.revokeTokenByValue(oldRefreshTokenValue);

    // Persist new refresh token in DB
    TokenDto refreshToken =
        tokenService.storeTokenOrThrow(userDto.getId(), generatedRefreshToken.getValue(),
            TokenType.REFRESH, generatedRefreshToken.getExpirationTime());

    // Generate access token
    JwtToken generatedAccessToken =
        jwtService.generateAccessTokenOrThrow(userDto.getUsername(), userDto.getId(),
            userDto.getRolesAsList(), userDto.getPermissionsAsList(), refreshToken.getId());

    // Map response DTO
    RefreshTokenResponse refreshTokenResponse =
        UserMapper.toResponse(generatedAccessToken.getValue(), generatedAccessToken.getExpiresIn(),
            generatedRefreshToken.getValue());

    log.info("New Refresh token fetching successful by userId={}", userDto.getId());

    return ResponseBuilder.successWithPayload("New Refresh token fetched successful",
        refreshTokenResponse);
  }


  @Override
  public ServiceResponse<?> forgotPassword(ServiceRequest<ForgotPasswordRequest> serviceRequest) {

    String clientIp = RequestContext.getClientIp();
    ForgotPasswordRequest payload = serviceRequest.getPayload();

    String email = payload.getEmail().trim().toLowerCase();

    log.info("Request from ip={} for email={}", clientIp, email);

    Optional<UserDto> userDtoOpt = userService.findByEmail(email);

    if (userDtoOpt.isEmpty()) {
      log.debug("Forgot password requested for non-existing email");
      /**
       * Do nothing and return a successful response. Do not expose this information to the client,
       * as it would enable user-enumeration attacks.
       */
      return ResponseBuilder.success("If the email exists, a password reset link has been sent.");
    }

    UserDto userDto = userDtoOpt.get();

    verificationService.sendVerification(userDto, VerificationIntent.PASSWORD_RESET,
        VerificationChannel.EMAIL);

    log.info("Forgot password flow triggered for userId={}", userDto.getId());

    return ResponseBuilder.success("If the email exists, a password reset link has been sent.");
  }


  @Override
  public ServiceResponse<?> resetPassword(ServiceRequest<ResetPasswordRequest> serviceRequest) {

    ResetPasswordRequest payload = serviceRequest.getPayload();
    log.info("Password reset attempt for email={}",
        payload.getEmail());

    // Finding user
    UserDto userDto = userService.findByEmail(payload.getEmail()).orElseThrow(() -> {
      log.debug("Reset password requested for non-existing email");
      // Do not expose details to user as it would enable user-enumeration attacks
      throw new InvalidTokenException("Invalid or expired password reset token");
    });

    // Verifying password-reset token
    verificationService.verifyCode(userDto.getId(), payload.getToken());

    // Updating password
    userService.updatePassword(userDto.getId(), payload.getNewPassword());

    log.info("Password reset successful for userId={}",
        userDto.getId());

    return ResponseBuilder.success("Password reset successfully");
  }


}
