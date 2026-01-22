package com.telemetry.engine.query.dto.response;

import java.time.Instant;
import lombok.Builder;


@Builder
public record AssetLocation(Long assetId, Long operatorId, double latitude,
    double longitude, double distanceMeters, boolean moving, double speed, double heading,
    Instant deviceTs) {

}
