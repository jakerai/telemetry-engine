package com.telemetry.engine.query.persistence.impl;

import java.time.Instant;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import com.telemetry.engine.query.dto.AssetLocationViewDto;
import com.telemetry.engine.query.enums.AssetStatus;
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
  public Flux<AssetLocationViewDto> findNearby(String assetType, double lat, double lon,
      double radiusMeters) {
    String sql = """
            SELECT acl.asset_id     AS asset_id,
                   a.name           AS asset_name,
                   a.model          AS asset_model,
                   a.status         AS asset_status,
                   acl.current_lat  AS current_lat,
                   acl.current_lon  AS current_lon,
                   acl.speed        AS speed,
                   acl.heading      AS heading,
                   acl.device_ts    AS device_ts
            FROM telemetry.asset_current_location acl
            INNER JOIN telemetry.asset a ON a.id = acl.asset_id
            INNER JOIN telemetry.asset_type at ON at.id = a.type_id
            WHERE at.code = $1
              AND ST_DWithin(
                    acl.location::geography,
                    ST_SetSRID(ST_MakePoint($2, $3), 4326)::geography,
                    $4
              )
            ORDER BY ST_Distance(
                    acl.location::geography,
                    ST_SetSRID(ST_MakePoint($2, $3), 4326)::geography
              ) ASC
        """;

    return databaseClient.sql(sql).bind("$1", assetType).bind("$2", lon).bind("$3", lat)
        .bind("$4", radiusMeters).<AssetLocationViewDto>map((row, meta) -> mapRowToDto(row)).all();
  }

  @Override
  public Mono<AssetLocationViewDto> findByAssetId(Long assetId) {
    String sql = """
            SELECT
                a.id            AS asset_id,
                a.name          AS asset_name,
                a.model         AS asset_model,
                a.status        AS asset_status,
                acl.current_lat AS current_lat,
                acl.current_lon AS current_lon,
                acl.speed       AS speed,
                acl.heading     AS heading,
                acl.device_ts   AS device_ts
            FROM telemetry.asset_current_location acl
            INNER JOIN telemetry.asset a ON a.id = acl.asset_id
            WHERE a.id = $1
        """;

    return databaseClient.sql(sql).bind("$1", assetId)
        .<AssetLocationViewDto>map((row, meta) -> mapRowToDto(row)).one();
  }

  private AssetLocationViewDto mapRowToDto(Row row) {

    String statusStr = row.get("asset_status", String.class);
    AssetStatus status = (statusStr != null) ? AssetStatus.valueOf(statusStr) : AssetStatus.UNKNOWN;

    return AssetLocationViewDto.builder().assetId(row.get("asset_id", Long.class))
        .name(row.get("asset_name", String.class)).model(row.get("asset_model", String.class))
        .status(status).currentLat(row.get("current_lat", Double.class))
        .currentLon(row.get("current_lon", Double.class)).speed(getSafeDouble(row, "speed"))
        .heading(getSafeDouble(row, "heading")).deviceTs(row.get("device_ts", Instant.class))
        .build();
  }

  private Double getSafeDouble(Row row, String column) {
    Double val = row.get(column, Double.class);
    return (val != null) ? val : 0.0;
  }

}
