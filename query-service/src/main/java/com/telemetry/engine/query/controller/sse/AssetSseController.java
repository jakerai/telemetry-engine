package com.telemetry.engine.query.controller.sse;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.query.dto.response.NearbyAsset;
import com.telemetry.engine.query.service.AssetService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path = "/api/v1/assets")
@RequiredArgsConstructor
public class AssetSseController {

  private final AssetService assetService;

  @GetMapping(value = "/sse/nearby", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<?>> streamNearbyAssets(@RequestParam Long assetTypeId,
      @RequestParam double lat, @RequestParam double lon, @RequestParam int radiusMeters) {

    return assetService.getNearbyAssets(lat, lon, assetTypeId, radiusMeters)
        .map(event -> ServerSentEvent
            .<NearbyAsset>builder().event(event.streamType().name().toLowerCase())
            .data(event.data()) /* null for heartbeat */
            .build());
  }

  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<NearbyAsset>> streamAssetCurrentLocation(
      @RequestParam Long assetId) {
    return assetService.trackAsset(assetId)
        .map(event -> ServerSentEvent.<NearbyAsset>builder()
            .event(event.streamType().name().toLowerCase()).data(event.data()).build());
  }

  
}
