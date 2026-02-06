package com.telemetry.engine.auth.core.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class MobileOauth2LoginRequest {
  
  @Pattern(regexp = "(?i)google|facebook",
      message = "Provider must be one of: google or facebook")
  @NotBlank(message = "Provider is required")
  private String provider;   
  
  @NotBlank(message = "Authentication token is missing. Ensure you are sending the correct token type for the chosen provider.")
  private String token;    
  
}
