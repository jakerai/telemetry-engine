package com.telemetry.engine.auth.core.apikey.dto;

import java.time.Instant;
import com.telemetry.engine.auth.common.base.dto.BaseDto;
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
public class ApiKeyDto extends BaseDto {
  private String key;
  private Instant expiresAt;
  private Long userId;
  private boolean active;
}
