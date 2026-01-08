package com.telemetry.engine.common.utils;

import com.telemetry.engine.common.dto.response.ResponseStatus;
import com.telemetry.engine.common.dto.response.ServiceResponse;

public class ResponseBuilder {
  private ResponseBuilder() {} // private constructor to prevent instantiation

  public static <T> ServiceResponse<T> success(String message) {
    return successWithStatusAndPayload(message, 200, null);
  }

  public static <T> ServiceResponse<T> successWithStatus(String message, int status) {
    return successWithStatusAndPayload(message, status, null);
  }

  public static <T> ServiceResponse<T> successWithPayload(String message, T payload) {
    return successWithStatusAndPayload(message, 200, payload);
  }

  public static <T> ServiceResponse<T> successWithStatusAndPayload(String message, int status,
      T payload) {
    return ServiceResponse.<T>builder().payload(payload)
        .status(ResponseStatus.builder().success(true).status(status).message(message).build())
        .build();
  }

  public static <T> ServiceResponse<T> error(String message, int status) {
    return ServiceResponse.<T>builder()
        .status(ResponseStatus.builder().success(false).status(status).message(message).build())
        .build();
  }

}
