package com.telemetry.engine.ingestion.dto;

import java.time.Instant;
import com.telemetry.engine.common.context.RequestContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageRequest {

  @Builder.Default
  private String requestId = RequestContext.getTraceId();

  private Long userId;

  private String clientIp;

  private Instant timestamp;

  private Object payload;

}
