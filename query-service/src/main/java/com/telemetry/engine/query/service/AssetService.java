package com.telemetry.engine.query.service;

import java.util.Map;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.query.dto.AssetLocationViewDto;
import reactor.core.publisher.Flux;

public interface AssetService {

  Flux<Map<String, Object>> getNearbyAssets(double lat, double lon, String type, int radius);

  Flux<ServiceResponse<AssetLocationViewDto>> streamAssetCurrentLocation(Long assetId);

}
