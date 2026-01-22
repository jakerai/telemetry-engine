package com.telemetry.engine.common.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record ServiceRequest<T>(@NotNull(message = "Payload cannot be null") @Valid T payload) {

  public static <T> ServiceRequest<T> of(T payload) {
    return new ServiceRequest<>(payload);
  }

}
