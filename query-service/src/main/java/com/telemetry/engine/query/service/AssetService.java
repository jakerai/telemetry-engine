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

  Mono<ServiceResponse<PagedResponse<AssetDto>>> getAssets(int page, int size, String createdBy,
      String modifiedBy, Long typeId, Long ownerId, String category, String sortDirection,
      String sortBy);

  Flux<NearbyAssetStreamEvent> getNearbyAssets(double lat, double lon, Long assetTypeId,
      int radius);

  Flux<AssetLocationStreamEvent> trackAsset(Long assetId);

}
