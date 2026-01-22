package com.telemetry.engine.common.dto.response;

import java.time.Instant;
import com.telemetry.engine.common.context.RequestContext;
import lombok.Builder;

@Builder
public record ServiceResponse<T>(boolean success, String message, int status, Object errors,
    Instant timestamp, String requestId, T data) {


  /* SUCCESS */
  public static <T> ServiceResponse<T> success(T data, String message) {
    return new ServiceResponse<>(true, message, 0, null, Instant.now(), RequestContext.getTraceId(),
        data);
  }

  public static <T> ServiceResponse<T> success(String message) {
    return success(null, message);
  }

  /* ERROR */

  public static <T> ServiceResponse<T> error(String message, Object errors) {
    return new ServiceResponse<>(false, message, -1, errors, Instant.now(),
        RequestContext.getTraceId(), null);
  }

  public static <T> ServiceResponse<T> error(String message) {
    return error(message, null);
  }

}
