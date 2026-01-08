package com.telemetry.engine.auth.core.apikey.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyRegistrationRequest {

  @NotBlank(message = "Asset ID is required")
  @Pattern(regexp = "^[A-Za-z0-9-]+$",
      message = "Asset ID must contain only letters, numbers, and hyphens")
  @Size(max = 50, message = "Asset ID must not exceed 50 characters")
  private String assetId;

  @NotNull(message = "Expires in days is required")
  @Min(value = 1, message = "Expires must be at least 1 day")
  @Max(value = 1825, message = "Expires cannot exceed 1825 days")
  private Integer expiresInDays;
  
  private Long userId; 
}
