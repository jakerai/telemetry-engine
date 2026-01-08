package com.telemetry.engine.auth.core.apikey.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyValidationRequest {

  @NotBlank(message = "API key is required")
  private String apiKey;

}
