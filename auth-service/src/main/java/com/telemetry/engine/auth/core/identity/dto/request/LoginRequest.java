package com.telemetry.engine.auth.core.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 50, message = "Password must be at least 8 characters long")
  private String password;
}
