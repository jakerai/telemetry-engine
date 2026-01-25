package com.telemetry.engine.query.persistence.impl;

import java.time.Instant;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import com.telemetry.engine.query.dto.response.NearbyAsset;
import com.telemetry.engine.query.persistence.AssetCurrentLocationPersistence;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetCurrentLocationPersistenceImpl implements AssetCurrentLocationPersistence {

  private final DatabaseClient databaseClient;


  @Override
  public Flux<NearbyAsset> findNearby(Long assetTypeId, double lat, double lon,
      double radiusMeters) {
    String sql = """
            SELECT acl.asset_id          AS asset_id,
                   at.code               AS asset_type,
                   acl.operator_id       AS operator_id,
                   acl.lat               AS latitude,
                   acl.lon               AS longitude,
                   acl.speed             AS speed,
                   acl.heading           AS heading,
                   acl.device_ts         AS device_ts,
                   ST_Distance(
                       acl.location::geography,
                       ST_SetSRID(ST_MakePoint($2, $3), 4326)::geography
                   ) AS distance_meters
            FROM telemetry.asset_current_location acl
            INNER JOIN telemetry.asset a ON a.id = acl.asset_id
            INNER JOIN telemetry.asset_type at ON at.id = a.type_id
            WHERE at.id = $1
              AND ST_DWithin(
                    acl.location::geography,
                    ST_SetSRID(ST_MakePoint($2, $3), 4326)::geography,
                    $4
              )
            ORDER BY distance_meters ASC
        """;

    return databaseClient.sql(sql).bind("$1", assetTypeId).bind("$2", lon).bind("$3", lat)
        .bind("$4", radiusMeters).<NearbyAsset>map((row, meta) -> mapRowToNearbyAsset(row)).all();
  }

  @Override
  public Mono<NearbyAsset> findByAssetId(Long assetId) {
    String sql = """
            SELECT acl.asset_id          AS asset_id,
                   at.code               AS asset_type,
                   acl.operator_id       AS operator_id,
                   acl.lat               AS latitude,
                   acl.lon               AS longitude,
                   acl.speed             AS speed,
                   acl.heading           AS heading,
                   acl.device_ts         AS device_ts,
                   0.0                   AS distance_meters
            FROM telemetry.asset_current_location acl
            INNER JOIN telemetry.asset a ON a.id = acl.asset_id
            INNER JOIN telemetry.asset_type at ON at.id = a.type_id
            WHERE a.id = $1
        """;

    return databaseClient.sql(sql).bind("$1", assetId)
        .<NearbyAsset>map((row, meta) -> mapRowToNearbyAsset(row)).one();
  }

  private NearbyAsset mapRowToNearbyAsset(Row row) {
    Long assetId = row.get("asset_id", Long.class);
    Long assetTypeId = row.get("asset_type", Long.class);
    Long operatorId = row.get("operator_id", Long.class);
    double latitude = row.get("latitude", Double.class);
    double longitude = row.get("longitude", Double.class);
    double distanceMeters = row.get("distance_meters", Double.class);
    double speed = getSafeDouble(row, "speed");
    double heading = getSafeDouble(row, "heading");
    boolean moving = speed > 0.0;
    Instant deviceTs = row.get("device_ts", Instant.class);

    return new NearbyAsset(assetId, assetTypeId, operatorId, latitude, longitude, distanceMeters,
        moving, speed, heading, deviceTs);
  }

  private Double getSafeDouble(Row row, String column) {
    Double val = row.get(column, Double.class);
    return (val != null) ? val : 0.0;
  }


}
