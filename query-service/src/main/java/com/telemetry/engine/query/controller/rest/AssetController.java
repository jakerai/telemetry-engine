package com.telemetry.engine.query.controller.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.query.dto.AssetDto;
import com.telemetry.engine.query.dto.response.PagedResponse;
import com.telemetry.engine.query.dto.resquest.AssetCreateRequest;
import com.telemetry.engine.query.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

  private final AssetService assetService;

  @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ServiceResponse<Void>> create(
      @RequestBody @Valid ServiceRequest<AssetCreateRequest> serviceRequest) {

    return assetService.createAsset(serviceRequest);
  }


  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ServiceResponse<PagedResponse<AssetDto>>> getAssets(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size,
          @RequestParam(required = false) String createdBy,
          @RequestParam(required = false) String modifiedBy,
          @RequestParam(required = false) Long typeId,
          @RequestParam(required = false) Long ownerId,
          @RequestParam(required = false) String category,
          @RequestParam(required = false, defaultValue = "DESC") String sortDirection,
          @RequestParam(required = false, defaultValue = "createdAt") String sortBy
  ) {
      return assetService.getAssets(page, size, createdBy, modifiedBy, typeId, ownerId, category,
              sortDirection, sortBy);
  }
 
}
