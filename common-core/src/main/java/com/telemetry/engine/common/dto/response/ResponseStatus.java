package com.telemetry.engine.common.dto.response;

import java.time.Instant;
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
public class ResponseStatus {
  private boolean success;
  private String message;
  private int status;
  private Object errors;
  @Builder.Default
  private Instant timestamp = Instant.now();
}
