package com.telemetry.engine.query.dto.response;

import java.time.Instant;
import lombok.Builder;

@Builder
public record NearbyAsset(Long assetId, String assetType, Long operatorId,
    double latitude, double longitude, double distanceMeters,
    boolean moving, double speed, double heading,
    Instant deviceTs) {

}
