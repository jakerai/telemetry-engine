package com.telemetry.engine.auth.core.user.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import com.telemetry.engine.auth.core.file.FileService;
import com.telemetry.engine.auth.core.identity.dto.request.SignupRequest;
import com.telemetry.engine.auth.core.identity.dto.response.MyProfileResponse;
import com.telemetry.engine.auth.core.identity.dto.response.PictureUploadResponse;
import com.telemetry.engine.auth.core.identity.dto.response.UserProfileResponse;
import com.telemetry.engine.auth.core.permission.entity.Permission;
import com.telemetry.engine.auth.core.role.contract.RoleAware;
import com.telemetry.engine.auth.core.role.entity.Role;
import com.telemetry.engine.auth.core.role.service.RoleService;
import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.auth.core.user.entity.UserOauthProvider;
import com.telemetry.engine.auth.core.user.mapper.UserMapper;
import com.telemetry.engine.auth.core.user.persistence.UserOauthProviderPersistence;
import com.telemetry.engine.auth.core.user.persistence.UserPersistence;
import com.telemetry.engine.auth.core.user.service.UserService;
import com.telemetry.engine.auth.security.model.AuthenticatedUser;
import com.telemetry.engine.auth.util.AuthUtil;
import com.telemetry.engine.auth.util.PasswordGenerator;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.exception.DuplicateResourceException;
import com.telemetry.engine.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final PasswordEncoder passwordEncoder;
  private final UserPersistence userPersistence;
  private final UserOauthProviderPersistence userOauthProviderPersistence;
  private final FileService fileStorageService;
  private final RoleService roleService;


  private User findByIdOrThrow(Long userId) {
    log.info("Finding user by user ID={}", userId);
    return userPersistence.findById(userId).orElseThrow(() -> {
      log.warn("User not found: user ID={}", userId);
      return new NotFoundException("User not found");
    });
  }


  private <T extends RoleAware> T enrichUserWithRoles(User user, T dto) {
    log.info("Enriching user ID={}", user.getId());

    Set<Role> userRoles = user.getRoles();

    // Extracting role names
    Set<String> roles =
        userRoles.stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());

    // Extracting permissions from roles
    Set<String> permissions = userRoles.stream().flatMap(r -> r.getPermissions().stream())
        .map(Permission::getType).collect(Collectors.toUnmodifiableSet());

    dto.setRoles(roles);
    dto.setPermissions(permissions);

    log.debug("User ID={} enriched with {} roles and {} permissions", user.getId(), roles.size(),
        permissions.size());

    return dto;
  }



  private User assignRole(User user, String roleName) {
    log.debug("Assigning role to user: user ID {}", user.getId());
    Role role = roleService.getRoleByNameOrThrow(roleName);
    user.getRoles().add(role);
    return userPersistence.save(user);
  }


  @Override
  public User findByUsernameOrThrow(String username) {
    Assert.hasText(username, "Username must not be empty");

    log.debug("Finding user by username: {}", username);

    return userPersistence.findByUsername(username).orElseThrow(() -> {
      log.warn("User lookup failed for username: {}", username);

      return new NotFoundException(String.format("User [%s] not found", username));
    });
  }


  @Override
  public Optional<UserDto> findByEmail(String email) {
    log.debug("Finding user by email: {}", email);
    Optional<User> userOpt = userPersistence.findByEmail(email);
    if (userOpt.isEmpty()) {
      log.warn("User not found for email={}", email);
      return Optional.empty();
    }
    return Optional.ofNullable(UserMapper.toUserDto(userOpt.get()));
  }


  @Override
  public UserDto createUserOrThrow(SignupRequest signupRequest) {
    log.info("Creating user with email={}", signupRequest.getEmail());

    Optional<User> userOpt = userPersistence.findByEmail(signupRequest.getEmail());

    if (!userOpt.isEmpty()) {
      log.warn("User already exists");
      throw new DuplicateResourceException(
          "An account with this email already exists. If this is your email, please sign in instead.");
    }

    // Save user
    User user = UserMapper.toEntity(signupRequest);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user = userPersistence.save(user);
    // Assign role
    assignRole(user, signupRequest.getUserType());

    log.info("User Created successfully: id={}, email={}", user.getId(), user.getPrimaryEmail());
    UserDto userDto = UserMapper.toUserDto(user);
    return enrichUserWithRoles(user, userDto);

  }


  @Override
  public UserDto updateLoginMetadataOrThrow(Long userId, String ip, Instant loginAt) {
    log.info("Updating login metadata for user ID={}", userId);

    User user = findByIdOrThrow(userId);
    user.setLastLoginAt(loginAt);
    user.setLastLoginIp(ip);
    user.setFailedLoginAttempts(0);

    User savedUser = userPersistence.save(user);
    log.info("Login metadata updated successfully for user ID={}", user.getId());
    UserDto userDto = UserMapper.toUserDto(user);
    return enrichUserWithRoles(savedUser, userDto);

  }


  @Override
  public ServiceResponse<MyProfileResponse> getMyProfile() {
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    log.info("Fetching user self profile with user ID={}", currentUser.getId());

    User user = findByIdOrThrow(currentUser.getId());
    MyProfileResponse data = MyProfileResponse.from(user);
    enrichUserWithRoles(user, data);
    return ServiceResponse.success(data, "Profile fetched successfully");
  }

  @Override
  public ServiceResponse<UserProfileResponse> getUserProfileById(Long userId) {
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    log.info("Fetching user by id={}", userId);

    User user = findByIdOrThrow(currentUser.getId());
    UserProfileResponse data = UserProfileResponse.from(user);

    enrichUserWithRoles(user, data);
    log.info("User profile retrieval successful for user ID={}", user.getId());

    return ServiceResponse.success(data, "User profile fetched successfully");
  }



  @Override
  public ServiceResponse<PictureUploadResponse> updateUserProfilePicture(MultipartFile file) {
    AuthenticatedUser currentUser = AuthUtil.getCurrentUserOrThrow();
    log.info("Profile picture upload request by user with user ID={}", currentUser.getId());

    User user = findByIdOrThrow(currentUser.getId());

    String filePath = fileStorageService.storeFile(String.valueOf(user.getId()), file);

    user.setPicture(filePath);
    user = userPersistence.save(user);
    log.info("Updated profile picture for user with user ID={} path={}", user.getId(), filePath);

    PictureUploadResponse data = PictureUploadResponse.builder().picture(user.getPicture()).build();

    return ServiceResponse.success(data, "Profile picture uploaded successfully");
  }

  public UserDto getUserOrThrow(Long userId) {

    User user = findByIdOrThrow(userId);
    UserDto userDto = UserMapper.toUserDto(user);
    return enrichUserWithRoles(user, userDto);
  }


  @Override
  public void markEmailVerified(Long userId) {
    log.info("Updating email as verified for user ID={}", userId);
    User user = findByIdOrThrow(userId);
    if (!user.isPrimaryEmailVerified()) {
      user.setPrimaryEmailVerified(true);
      userPersistence.save(user);
    }
  }


  @Override
  public void markMobileNumberVerified(Long userId) {
    log.info("Updating mobile number as verified for user ID={}", userId);
    User user = findByIdOrThrow(userId);
    if (!user.isPrimaryMobileNumberVerified()) {
      user.setPrimaryMobileNumberVerified(true);
      userPersistence.save(user);
    }
  }


  @Override
  public void updatePassword(Long userId, String newPassword) {
    log.info("Updating password for user ID={}", userId);
    User user = findByIdOrThrow(userId);
    user.setPassword(passwordEncoder.encode(newPassword));
    user = userPersistence.save(user);
  }


  @Override
  public UserDto findOrCreateOAuthUser(String email, String provider, String providerUserId,
      String firstName, String lastName, String role) {
    log.info("Creating user with email={} for provider={}", email, provider);

    Optional<User> userOpt = userPersistence.findByEmail(email);
    User user = null;
    if (userOpt.isEmpty()) {
      log.warn("User new User");
      user = User.builder().firstName(firstName).lastName(lastName).primaryEmail(email)
          .username(email).password(PasswordGenerator.generate()).build();
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      user = userPersistence.save(user);
      assignRole(user, role);

      log.info("User Created successfully: id={}, email={}", user.getId(), user.getPrimaryEmail());
    } else {
      log.warn("Existing User");
      user = userOpt.get();
    }

    Optional<UserOauthProvider> userOauthProviderOpt =
        userOauthProviderPersistence.findByUserIdAndProvider(user.getId(), provider);
    /* Save UserOauthProvider only if the data not found for provided provider */
    if (userOauthProviderOpt.isEmpty()) {
      UserOauthProvider userOauthProvider = UserOauthProvider.builder().user(user)
          .provider(provider).providerUserId(providerUserId).build();
      userOauthProviderPersistence.save(userOauthProvider);
    }

    UserDto userDto = UserMapper.toUserDto(user);
    return enrichUserWithRoles(user, userDto);
  }

}
