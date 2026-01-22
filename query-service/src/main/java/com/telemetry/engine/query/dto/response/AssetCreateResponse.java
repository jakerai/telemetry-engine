package com.telemetry.engine.query.dto.response;

import com.telemetry.engine.query.enums.AssetStatus;
import lombok.Builder;

@Builder
public record AssetCreateResponse(String name, String model, Long typeId, AssetStatus status,
    Long ownerId, Long operatorId) {
}
