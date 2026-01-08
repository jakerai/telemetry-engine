package com.telemetry.engine.auth.core.user.mapper;

import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.dto.response.MyProfileResponse;
import com.telemetry.engine.auth.core.identity.dto.response.RefreshTokenResponse;
import com.telemetry.engine.auth.core.identity.dto.response.UserProfileResponse;
import com.telemetry.engine.auth.core.role.config.RoleInitializer;
import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.common.exception.NotFoundException;
import com.telemetry.engine.common.utils.SafeExtractUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserMapper {

  public static User toEntity(SignupRequest dto) {
    if (dto == null) {
      return null;
  }

  String requestedRole = dto.getUserType();

  if (requestedRole == null || requestedRole.isBlank()) {
      throw new NotFoundException("userType is required");
  }

  requestedRole = requestedRole.trim().toUpperCase();

  // Validating against existing roles 
  if (!RoleInitializer.getAllRoles().contains(requestedRole)) {
      log.info("Invalid userType={} : valid types are {}",
              requestedRole, RoleInitializer.getAllRoles());

      throw new NotFoundException("Invalid userType: " + requestedRole);
  }

  return User.builder()
          .username(dto.getEmail())
          .email(dto.getEmail())
          .password(dto.getPassword())
          .firstName(dto.getFirstName())
          .lastName(dto.getLastName())
          .mobileNumber(dto.getMobileNumber())
          .build();
  }


  public static UserDto toUserDto(User entity) {
    if (entity == null)
      return null;

    return UserDto.builder().id(entity.getId()).username(entity.getEmail()).email(entity.getEmail())
        .firstName(entity.getFirstName()).status(entity.getStatus())
        .createdAt(entity.getCreatedAt()).modifiedAt(entity.getModifiedAt()).build();
  }

  public static LoginResponse toUserLoginResponse(String accessToken, Long accessTokenExpiresIn,
      String refreshToken, UserDto dto) {

    if (dto == null)
      return null;

    return LoginResponse.builder().accessToken(accessToken)
        .accessTokenExpiresIn(accessTokenExpiresIn).refreshToken(refreshToken).tokenType("Bearer")
        .user(dto).build();
  }


  public static RefreshTokenResponse toResponse(String accessToken, Long accessTokenExpiresIn,
      String refreshToken) {
    return RefreshTokenResponse.builder().accessToken(accessToken)
        .accessTokenExpiresIn(accessTokenExpiresIn).refreshToken(refreshToken).tokenType("Bearer")
        .build();
  }

  public static MyProfileResponse toMyProfileResponse(User user) {
    if (user == null)
      return null;

    return MyProfileResponse.builder().id(SafeExtractUtil.safeGet(() -> user.getId(), null))
        .username(SafeExtractUtil.safeGet(() -> user.getUsername(), ""))
        .email(SafeExtractUtil.safeGet(() -> user.getEmail(), ""))
        .picture(SafeExtractUtil.safeGet(() -> user.getPicture(), ""))
        .firstName(SafeExtractUtil.safeGet(() -> user.getFirstName(), ""))
        .lastName(SafeExtractUtil.safeGet(() -> user.getLastName(), ""))
        .status(SafeExtractUtil.safeGet(() -> user.getStatus(), null))
        .lockedAt(SafeExtractUtil.safeGet(() -> user.getLockedAt(), null))
        .lastLoginAt(SafeExtractUtil.safeGet(() -> user.getLastLoginAt(), null))
        .createdAt(SafeExtractUtil.safeGet(() -> user.getCreatedAt(), null))
        .modifiedAt(SafeExtractUtil.safeGet(() -> user.getModifiedAt(), null)).build();

  }

  public static UserProfileResponse toUserProfileResponse(User user) {
    if (user == null)
      return null;

    return UserProfileResponse.builder().id(SafeExtractUtil.safeGet(() -> user.getId(), null))
        .username(SafeExtractUtil.safeGet(() -> user.getUsername(), ""))
        .email(SafeExtractUtil.safeGet(() -> user.getEmail(), ""))
        .picture(SafeExtractUtil.safeGet(() -> user.getPicture(), ""))
        .firstName(SafeExtractUtil.safeGet(() -> user.getFirstName(), ""))
        .lastName(SafeExtractUtil.safeGet(() -> user.getLastName(), ""))
        .status(SafeExtractUtil.safeGet(() -> user.getStatus(), null))
        .lockedAt(SafeExtractUtil.safeGet(() -> user.getLockedAt(), null))
        .lastLoginAt(SafeExtractUtil.safeGet(() -> user.getLastLoginAt(), null))
        .createdAt(SafeExtractUtil.safeGet(() -> user.getCreatedAt(), null))
        .modifiedAt(SafeExtractUtil.safeGet(() -> user.getModifiedAt(), null)).build();
  }

}
