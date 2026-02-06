package com.telemetry.engine.auth.core.user.service;

import java.time.Instant;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.MyProfileResponse;
import com.telemetry.engine.auth.core.identity.dto.response.PictureUploadResponse;
import com.telemetry.engine.auth.core.identity.dto.response.UserProfileResponse;
import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.common.dto.response.ServiceResponse;

public interface UserService {

  User findByUsernameOrThrow(String username);

  Optional<UserDto> findByEmail(String email);

  UserDto createUserOrThrow(SignupRequest signupRequest);

  UserDto updateLoginMetadataOrThrow(Long userId, String ip, Instant loginAt);

  ServiceResponse<MyProfileResponse> getMyProfile();

  ServiceResponse<UserProfileResponse> getUserProfileById(Long userId);

  ServiceResponse<PictureUploadResponse> updateUserProfilePicture(MultipartFile file);

  UserDto getUserOrThrow(Long userId);

  void markEmailVerified(Long userId);

  void markMobileNumberVerified(Long userId);

  void updatePassword(Long userId, String newPassword);

  UserDto findOrCreateOAuthUser(String email, String provider, String providerUserId,
      String firstName, String lastName, String role);

}
