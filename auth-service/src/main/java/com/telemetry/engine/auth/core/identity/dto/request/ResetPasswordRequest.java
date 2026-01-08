package com.telemetry.engine.auth.core.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ResetPasswordRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;
  
  @NotBlank(message = "Token is required")
  @Size(min = 6, max = 100, message = "Token must be at least 6 characters long")
  private String token;

  @NotBlank(message = "New Password is required")
  @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
      message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
  private String newPassword;
}
