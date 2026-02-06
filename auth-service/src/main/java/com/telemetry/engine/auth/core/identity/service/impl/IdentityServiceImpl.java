package com.telemetry.engine.auth.core.identity.service.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.telemetry.engine.auth.constants.RoleConstants;
import com.telemetry.engine.auth.core.activity.enums.Action;
import com.telemetry.engine.auth.core.activity.service.ActivityService;
import com.telemetry.engine.auth.core.identity.dto.request.LoginRequest;
import com.telemetry.engine.auth.core.identity.dto.request.MobileOauth2LoginRequest;
import com.telemetry.engine.auth.core.identity.dto.request.PasswordForgotRequest;
import com.telemetry.engine.auth.core.identity.dto.request.PasswordResetRequest;
import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.dto.response.RefreshTokenResponse;
import com.telemetry.engine.auth.core.identity.service.IdentityService;
import com.telemetry.engine.auth.core.token.dto.TokenDto;
import com.telemetry.engine.auth.core.token.service.TokenService;
import com.telemetry.engine.auth.core.user.dto.UserDto;
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
  public ServiceResponse<Void> signup(ServiceRequest<SignupRequest> serviceRequest) {
    String clientIp = RequestContext.getClientIp();

    SignupRequest signupRequest = serviceRequest.payload();
    log.info("New user signup for email={} from ip={}", signupRequest.getEmail(), clientIp);

    UserDto userDto = userService.createUserOrThrow(signupRequest);

    log.info("New User signed up successfully with ID={}, email={}", userDto.getId(),
        userDto.getEmail());

    verificationService.sendVerification(userDto, VerificationIntent.SIGNUP,
        VerificationChannel.EMAIL);
    return ServiceResponse.success("Signup successfully");
  }


  @Override
  public ServiceResponse<LoginResponse> login(ServiceRequest<LoginRequest> serviceRequest) {

    String clientIp = RequestContext.getClientIp();
    LoginRequest loginRequest = serviceRequest.payload();
    log.info("Login request for email={} from ip={}", loginRequest.getEmail(), clientIp);

    AuthenticatedUser user = authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
    LoginResponse data = buildLoginResponseOnSuccess(user.getId(), clientIp);

    log.info("Login successful for email: {}", user.getUser().getPrimaryEmail());

    return ServiceResponse.success(data, "Login successfully");
  }

  @Transactional
  @Override
  public ServiceResponse<LoginResponse> loginOauth2Web(OAuth2User oauth2User,
      OAuth2AuthenticationToken authentication) {

    String clientIp = RequestContext.getClientIp();
    log.info("Oauth2 login request for from ip={}", clientIp);
    
    String provider = authentication.getAuthorizedClientRegistrationId();
    
    /* Extracting profile safely (firstName, lastName, email, providerUserId) */
    Map<String, String> profile = extractOAuthProfile(provider, oauth2User);

    String email = profile.get("email");
    if (email == null) {
      throw new OAuth2AuthenticationException("Email not found from provider: " + provider);
    }

    String firstName = profile.get("firstName");
    String lastName = profile.get("lastName");
    String providerUserId = profile.get("providerUserId");

    /* Sync or create user in DB */
    UserDto user = userService.findOrCreateOAuthUser(email, provider, providerUserId, firstName,
        lastName, RoleConstants.DEFAULT_USER_ROLE);

    LoginResponse data = buildLoginResponseOnSuccess(user.getId(), clientIp);

    log.info("Oauth2 Login successful for email: {}", user.getEmail());
    return ServiceResponse.success(data, "Oauth2 login successfully");
  }
  
  
  @Override
  public ServiceResponse<LoginResponse> loginOauth2Mobile(
      ServiceRequest<MobileOauth2LoginRequest> serviceRequest) {
   
    return null;
  }


  private Map<String, String> extractOAuthProfile(String provider, OAuth2User user) {

    Map<String, String> profile = new HashMap<>();

    String firstName = null;
    String lastName = null;
    String email = null;
    String providerUserId = null;

    if ("google".equals(provider)) {
      providerUserId = user.getAttribute("sub"); // Google ID
      email = user.getAttribute("email");
      firstName = user.getAttribute("given_name");
      lastName = user.getAttribute("family_name");
    }

    else if ("facebook".equals(provider)) {
      providerUserId = user.getAttribute("id");
      email = user.getAttribute("email");
      firstName = user.getAttribute("first_name");
      lastName = user.getAttribute("last_name");
    }

    else if ("github".equals(provider)) {
      Object id = user.getAttribute("id");
      providerUserId = id != null ? String.valueOf(id) : null;

      email = user.getAttribute("email");

      String name = user.getAttribute("name");
      if (name != null && !name.isBlank()) {
        int idx = name.indexOf(' ');
        if (idx > 0) {
          firstName = name.substring(0, idx);
          lastName = name.substring(idx + 1);
        } else {
          firstName = name;
        }
      } else {
        firstName = user.getAttribute("login");
      }
    }

    profile.put("provider", provider);
    profile.put("providerUserId", providerUserId);
    profile.put("email", email);
    profile.put("firstName", firstName);
    profile.put("lastName", lastName);

    return profile;
  }
  
  private LoginResponse buildLoginResponseOnSuccess(Long userId, String clientIp) {
    UserDto userDto = userService.updateLoginMetadataOrThrow(userId, clientIp, Instant.now());

    activityService.logActivity(Action.LOGIN_SUCCESS, userDto.getId(), clientIp);

    JwtToken generatedRefreshToken =
        jwtService.generateRefreshTokenOrThrow(userDto.getUsername(), userDto.getId());

    TokenDto refreshToken =
        tokenService.storeTokenOrThrow(userDto.getId(), generatedRefreshToken.getValue(),
            TokenType.REFRESH, generatedRefreshToken.getExpirationTime());

    JwtToken generatedAccessToken =
        jwtService.generateAccessTokenOrThrow(userDto.getUsername(), userDto.getId(),
            userDto.getRolesAsList(), userDto.getPermissionsAsList(), refreshToken.getId());

    return LoginResponse.from(generatedAccessToken.getValue(), generatedAccessToken.getExpiresIn(),
        generatedRefreshToken.getValue(), userDto);
  }

  @Override
  public ServiceResponse<Void> logout() {

    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    String clientIp = RequestContext.getClientIp();
    Long refreshTokenId = RequestContext.getRefreshTokenId();
    log.info("Logout request for user ID={} from ip={}", currentUser.getId(), clientIp);

    tokenService.revokeTokenById(refreshTokenId);
    activityService.logActivity(Action.LOGOUT, currentUser.getId(), clientIp);

    log.info("Logout successful for userId={}", currentUser.getId());
    return ServiceResponse.success("Logout successfully");
  }

  @Override
  public ServiceResponse<RefreshTokenResponse> refreshToken() {

    String clientIp = RequestContext.getClientIp();
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    log.info("New refresh token request by user ID= {} from ip={}", currentUser.getId(), clientIp);
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
    RefreshTokenResponse data = RefreshTokenResponse.from(generatedAccessToken.getValue(),
        generatedAccessToken.getExpiresIn(), generatedRefreshToken.getValue());

    log.info("New Refresh token fetching successful by userId={}", userDto.getId());

    return ServiceResponse.success(data, "Refresh token fetched successfully");
  }


  @Override
  public ServiceResponse<Void> forgotPassword(
      ServiceRequest<PasswordForgotRequest> serviceRequest) {

    String clientIp = RequestContext.getClientIp();
    PasswordForgotRequest passwordForgotRequest = serviceRequest.payload();
    String email = passwordForgotRequest.getEmail().trim().toLowerCase();

    log.info("Request from ip={} for email={}", clientIp, email);

    Optional<UserDto> userDtoOpt = userService.findByEmail(email);

    if (userDtoOpt.isEmpty()) {
      log.debug("Forgot password requested for non-existing email");
      /**
       * Do nothing and return a successful response. Do not expose this information to the client,
       * as it would enable user-enumeration attacks.
       */
      return ServiceResponse
          .success("If an account with this email exists, a password reset link has been sent.");
    }

    UserDto userDto = userDtoOpt.get();

    verificationService.sendVerification(userDto, VerificationIntent.PASSWORD_RESET,
        VerificationChannel.EMAIL);

    log.info("Forgot password flow triggered for userId={}", userDto.getId());
    return ServiceResponse.success("Forgot password requested successfully");
  }


  @Override
  public ServiceResponse<Void> resetPassword(ServiceRequest<PasswordResetRequest> serviceRequest) {

    PasswordResetRequest passwordResetRequest = serviceRequest.payload();
    log.info("Password reset attempt for email={}", passwordResetRequest.getEmail());

    // Finding user
    UserDto userDto = userService.findByEmail(passwordResetRequest.getEmail()).orElseThrow(() -> {
      log.debug("Reset password requested for non-existing email");
      // Do not expose details to user as it would enable user-enumeration attacks
      throw new InvalidTokenException("Invalid or expired password reset token");
    });

    // Verifying password-reset token
    verificationService.verifyCode(userDto.getId(), passwordResetRequest.getToken());

    // Updating password
    userService.updatePassword(userDto.getId(), passwordResetRequest.getNewPassword());

    log.info("Password reset successful for userId={}", userDto.getId());
    return ServiceResponse.success("Password reset requested successfully");
  }

  
}
