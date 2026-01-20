package com.telemetry.engine.consumer.persistence.impl;

import java.util.List;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import com.telemetry.engine.consumer.entity.AssetLocationHistory;
import com.telemetry.engine.consumer.persistence.AssetLocationHistoryPersistence;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class AssetLocationHistoryPersistenceImpl implements AssetLocationHistoryPersistence {

  private final DatabaseClient databaseClient;

  @Override
  public Mono<AssetLocationHistory> save(AssetLocationHistory assetLocationEvent) {
    return insert(assetLocationEvent).thenReturn(assetLocationEvent);
  }

  /**
   * Insert a list of AssetLocationHistory into DB using PostGIS geography for location.
   */
  public Flux<AssetLocationHistory> insertAll(List<AssetLocationHistory> events) {
    if (events == null || events.isEmpty()) {
      return Flux.empty();
    }

    return Flux.fromIterable(events).flatMap(event -> insert(event).thenReturn(event));
  }

  /**
   * Insert a single AssetLocationHistory into DB.
   */
  private Mono<Void> insert(AssetLocationHistory event) {
    String sql =
        """
            INSERT INTO telemetry.asset_location_history
                (device_ts, asset_id, latitude, longitude, location, h3_index, speed, heading, processed_at)
            VALUES ($1, $2, $3, $4, ST_SetSRID(ST_MakePoint($4, $3), 4326)::geography, $5, $6, $7, $8)
            """;

    var spec =
        databaseClient.sql(sql).bind("$1", event.getDeviceTs()).bind("$2", event.getAssetId())
            .bind("$3", event.getLatitude()).bind("$4", event.getLongitude());

    if (event.getH3Index() != null) {
      spec = spec.bind("$5", event.getH3Index());
    } else {
      spec = spec.bindNull("$5", String.class);
    }

    if (event.getSpeed() != null) {
      spec = spec.bind("$6", event.getSpeed());
    } else {
      spec = spec.bindNull("$6", Double.class);
    }

    if (event.getHeading() != null) {
      spec = spec.bind("$7", event.getHeading());
    } else {
      spec = spec.bindNull("$7", Double.class);
    }

    if (event.getProcessedAt() != null) {
      spec = spec.bind("$8", event.getProcessedAt());
    } else {
      spec = spec.bindNull("$8", java.time.Instant.class);
    }

    return spec.then();
  }

}
