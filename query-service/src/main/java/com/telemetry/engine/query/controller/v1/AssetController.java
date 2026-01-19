package com.telemetry.engine.query.controller.v1;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.query.dto.AssetLocationViewDto;
import com.telemetry.engine.query.service.AssetService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path = "/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

  private final AssetService assetService;

  @GetMapping(value = "/sse/nearby", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<?>> streamNearbyAssets(@RequestParam String assetType,
      @RequestParam double lat, @RequestParam double lon, @RequestParam int radiusMeters) {

    return assetService.getNearbyAssets(lat, lon, assetType, radiusMeters).map(data -> {
      String eventType = (String) data.getOrDefault("streamType", "delta");
      return ServerSentEvent.builder().event(eventType).data(data)
          .build(); /* eventType will be snapshot, delta, or heartbeat */


    });
  }

  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServiceResponse<AssetLocationViewDto>> streamAssetCurrentLocation(
      @RequestParam Long assetId) {
    return assetService.streamAssetCurrentLocation(assetId);
  }

}
