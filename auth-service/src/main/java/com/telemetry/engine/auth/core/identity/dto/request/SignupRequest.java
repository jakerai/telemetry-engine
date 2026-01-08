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
public class SignupRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
      message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
  private String password;

  @NotBlank(message = "UserType is required")
  @Pattern(regexp = "USER",
      message = "User type must be one of: USER")
  private String userType;

  @NotBlank(message = "First Name is required")
  @Pattern(regexp = "^[A-Za-z]+$", message = "First Name must contain only letters")
  @Size(max = 50, message = "First Name must not exceed 50 characters")
  private String firstName;

  @NotBlank(message = "Last Name is required")
  @Pattern(regexp = "^[A-Za-z]+$", message = "Last Name must contain only letters")
  @Size(max = 50, message = "Last Name must not exceed 50 characters")
  private String lastName;
  
  @Size(max = 15, message = "Mobile number cannot be longer than 15 digits")
  @Pattern(regexp = "^\\+?[0-9]*$", message = "Mobile number must contain only digits and optional leading +")
  private String mobileNumber;

}
