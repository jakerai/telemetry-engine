package com.telemetry.engine.query.dto.response;

import java.time.Instant;
import com.telemetry.engine.common.redis.model.AssetRedisState;
import lombok.Builder;

@Builder
public record NearbyAsset(Long assetId, Long assetTypeId, Long operatorId,
    double latitude, double longitude, double distanceMeters,
    boolean moving, double speed, double heading,
    Instant deviceTs) {

  public static NearbyAsset from(AssetRedisState assetState) {
    if (assetState == null) {
        return null;
    }

    double speed = assetState.getSpeed();
    double latitude = assetState.getLatitude();
    double longitude = assetState.getLongitude();
    double heading = assetState.getHeading();
    Instant deviceTs = assetState.getDeviceTs() != null ? assetState.getDeviceTs() : Instant.now();
    

    return new NearbyAsset(
            assetState.getAssetId(),
            assetState.getAssetTypeId(),
            assetState.getOperatorId(),
            latitude,
            longitude,
            0.0,           // distanceMeters: default 0.0, can compute later if needed
            speed > 0,     // moving: true if speed > 0
            speed,
            heading,
            deviceTs
    );
}
}
