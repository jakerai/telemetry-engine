package com.telemetry.engine.auth.security.model;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import com.telemetry.engine.auth.core.permission.entity.Permission;
import com.telemetry.engine.auth.core.role.entity.Role;
import com.telemetry.engine.auth.core.user.entity.User;
import com.telemetry.engine.auth.core.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
@AllArgsConstructor
public class AuthenticatedUser implements UserDetails {
  private static final long serialVersionUID = 1L;

  private final Long id;
  private final String username;
  private final UserStatus status;
  private Jwt currentJwt;
  private final User user;

  public AuthenticatedUser(User user) {
    this.user = user;
    this.id = user.getId();
    this.username = user.getUsername();
    this.status = user.getStatus();
  }


  public Set<String> getRoles() {
    return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
  }

  public Set<String> getPermissions() {
    Set<Role> userRoles = user.getRoles();

    return userRoles.stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getType)
        .collect(Collectors.toUnmodifiableSet());
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream().map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return status != UserStatus.LOCKED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return status == UserStatus.ACTIVE || status == UserStatus.PENDING_VERIFICATION;
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }


}

