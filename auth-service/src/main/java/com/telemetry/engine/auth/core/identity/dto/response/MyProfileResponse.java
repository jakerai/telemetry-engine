package com.telemetry.engine.auth.core.identity.dto.response;

import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.common.utils.SafeExtractUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class MyProfileResponse extends UserProfileResponse {

  public static MyProfileResponse from(User user) {
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

}
