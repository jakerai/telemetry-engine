package com.telemetry.engine.auth.core.user.dto;

import java.util.Set;
import com.telemetry.engine.auth.common.base.dto.BaseDto;
import com.telemetry.engine.auth.core.role.contract.RoleAware;
import com.telemetry.engine.auth.core.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;



@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class UserDto extends BaseDto implements RoleAware {
  private String email;
  private String mobileNumber;
  private String newEmail;
  private String newMobileNumber;
  private String username;
  private String firstName;
  private UserStatus status;
  private Set<String> roles;
  private Set<String> permissions;

}
