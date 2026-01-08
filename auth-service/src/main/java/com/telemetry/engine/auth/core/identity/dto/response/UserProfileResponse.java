package com.telemetry.engine.auth.core.identity.dto.response;

import java.time.Instant;
import com.telemetry.engine.auth.core.user.dto.UserDto;
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
}
