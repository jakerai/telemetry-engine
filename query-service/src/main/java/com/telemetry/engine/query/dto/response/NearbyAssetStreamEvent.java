package com.telemetry.engine.query.dto.response;

import com.telemetry.engine.query.enums.StreamType;
import lombok.Builder;

@Builder
public record NearbyAssetStreamEvent(StreamType streamType, NearbyAsset data) {

}
