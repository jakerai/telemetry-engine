package com.telemetry.engine.auth.core.user.mapper;

import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.dto.response.RefreshTokenResponse;
import com.telemetry.engine.auth.core.role.config.RoleInitializer;
import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.common.exception.NotFoundException;
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

  
}
