package com.telemetry.engine.auth.core.identity.dto.response;

import java.time.Instant;
import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.common.utils.SafeExtractUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserProfileResponse extends UserDto {
  private String picture;
  private String firstName;
  private String lastName;
  private Instant lockedAt;
  private Instant lastLoginAt;

  public static UserProfileResponse from(User user) {
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
