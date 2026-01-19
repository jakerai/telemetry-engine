package com.telemetry.engine.consumer.persistence.impl;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import com.telemetry.engine.consumer.entity.AssetCurrentLocation;
import com.telemetry.engine.consumer.persistence.AssetCurrentLocationPersistence;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetCurrentLocationPersistenceImpl implements AssetCurrentLocationPersistence {

  /**
   * R2DBC does not support ON CONFLICT via annotations so we use DatabaseClient
   */
  private final DatabaseClient databaseClient;


  /**
   * Upserts the latest location of an asset into the {@code telemetry.asset_current_location}
   * table.
   *
   * <p>
   * This table maintains exactly one row per asset representing its most recent known location. It
   * is optimized for ultra-fast read queries such as live maps, tracking dashboards, and real-time
   * monitoring views.
   * </p>
   *
   * <h3>Behavior</h3>
   * <ul>
   * <li>If a row for the given {@code asset_id} does not exist, a new row is inserted.</li>
   * <li>If a row for the given {@code asset_id} already exists, it is updated with the latest
   * values.</li>
   * </ul>
   *
   * <h3>Required Table Design</h3>
   * <p>
   * For this method to work correctly, the database table <b>must</b> satisfy the following:
   * </p>
   *
   * <pre>
   * 1. {@code
   * asset_id
   * } must be the PRIMARY KEY (required for {@code ON CONFLICT (asset_id)} to function)
   *
   * 2. Table must exist as:
   *    telemetry.asset_current_location
   *
   * 3. Required columns:
   *    - asset_id     BIGINT PRIMARY KEY
   *    - current_lat  DOUBLE PRECISION NOT NULL
   *    - current_lon  DOUBLE PRECISION NOT NULL
   *    - location     GEOGRAPHY(Point, 4326) NOT NULL
   *    - speed        DOUBLE PRECISION
   *    - heading      DOUBLE PRECISION
   *    - modified_at  TIMESTAMPTZ (updated via trigger)
   *
   * 4. A BEFORE UPDATE trigger must exist to automatically update {@code
   * modified_at
   * }
   * </pre>
   *
   * <h3>Implementation Notes</h3>
   * <ul>
   * <li>Uses PostgreSQL {@code ON CONFLICT (asset_id)} clause for atomic upsert semantics.</li>
   * <li>Relies on {@code asset_id} being the PRIMARY KEY (not just UNIQUE).</li>
   * <li>Uses {@link org.springframework.r2dbc.core.DatabaseClient} because R2DBC repositories do
   * not support {@code ON CONFLICT} natively.</li>
   * <li>Coordinates are stored using PostGIS {@code GEOGRAPHY(Point, 4326)} type for efficient
   * spatial queries.</li>
   * <li>{@code modified_at} is handled by a database trigger, not by application code.</li>
   * </ul>
   *
   * @param loc the latest location snapshot of the asset to be upserted
   * @return a {@link reactor.core.publisher.Mono} that completes when the operation finishes
   */
  @Override
  public Mono<Void> upsert(AssetCurrentLocation loc) {
    String sql = """
            INSERT INTO telemetry.asset_current_location
            (asset_id, current_lat, current_lon, location, speed, heading, device_ts, processed_at)
            VALUES ($1, $2, $3, point($3, $2), $4, $5, $6, $7)
            ON CONFLICT (asset_id)
            DO UPDATE SET
                current_lat  = EXCLUDED.current_lat,
                current_lon  = EXCLUDED.current_lon,
                location     = EXCLUDED.location,
                speed        = EXCLUDED.speed,
                heading      = EXCLUDED.heading,
                device_ts    = EXCLUDED.device_ts,
                processed_at = EXCLUDED.processed_at
            WHERE EXCLUDED.device_ts > asset_current_location.device_ts
        """;

    return databaseClient.sql(sql).bind("$1", loc.getAssetId()).bind("$2", loc.getCurrentLat())
        .bind("$3", loc.getCurrentLon()).bind("$4", loc.getSpeed()).bind("$5", loc.getHeading())
        .bind("$6", loc.getDeviceTs()) 
        .bind("$7", loc.getProcessedAt()) // Process starting time by producer
        .then();
  }

}
