package com.telemetry.engine.query.service;

import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.query.dto.AssetDto;
import com.telemetry.engine.query.dto.response.AssetLocationStreamEvent;
import com.telemetry.engine.query.dto.response.AssetUpdateResponse;
import com.telemetry.engine.query.dto.response.NearbyAssetStreamEvent;
import com.telemetry.engine.query.dto.response.PagedResponse;
import com.telemetry.engine.query.dto.resquest.AssetCreateRequest;
import com.telemetry.engine.query.dto.resquest.AssetUpdateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetService {

  Mono<ServiceResponse<Void>> createAsset(ServiceRequest<AssetCreateRequest> serviceRequest);

  Mono<ServiceResponse<AssetDto>> getAsset(Long assetId);

  Mono<ServiceResponse<AssetUpdateResponse>> updateAsset(Long assetId, AssetUpdateRequest request);
  
  Mono<ServiceResponse<PagedResponse<AssetDto>>> getAssets(int page, int size);

  Mono<ServiceResponse<PagedResponse<AssetDto>>> getAssetsByOwnerId(Long ownerId, int page, int size);


  Flux<NearbyAssetStreamEvent> getNearbyAssets(double lat, double lon, String type, int radius);

  Flux<AssetLocationStreamEvent> streamAssetCurrentLocation(Long assetId);

}
