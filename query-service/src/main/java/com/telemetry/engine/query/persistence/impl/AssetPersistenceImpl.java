package com.telemetry.engine.query.persistence.impl;

import java.util.function.BiFunction;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import com.telemetry.engine.query.dto.AssetDto;
import com.telemetry.engine.query.dto.resquest.AssetFilter;
import com.telemetry.engine.query.entity.Asset;
import com.telemetry.engine.query.enums.AssetStatus;
import com.telemetry.engine.query.persistence.AssetPersistence;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

@Component
@RequiredArgsConstructor
public class AssetPersistenceImpl implements AssetPersistence {

  private final DatabaseClient db;

  @Override
  public Mono<Asset> save(Asset asset) {
    String sql = """
        INSERT INTO telemetry.asset (name, model, serial_number, type_id, status, owner_id)
        VALUES (:name, :model, :serialNumber, :typeId, :status, :ownerId)
        ON CONFLICT(id) DO UPDATE SET
            name = :name,
            model = :model,
            serial_number = :serialNumber,
            type_id = :typeId,
            status = :status,
            owner_id = :ownerId
        RETURNING *
        """;

    return db.sql(sql).bind("name", asset.getName()).bind("model", asset.getModel())
        .bind("serialNumber", asset.getSerialNumber()).bind("typeId", asset.getTypeId())
        .bind("status", asset.getStatus() != null ? asset.getStatus().name() : null)
        .bind("ownerId", asset.getOwnerId())
        .map((BiFunction<Row, RowMetadata, Asset>) (row, meta) -> Asset.builder()
            .id(row.get("id", Long.class)).name(row.get("name", String.class))
            .model(row.get("model", String.class))
            .serialNumber(row.get("serial_number", String.class))
            .typeId(row.get("type_id", Long.class))
            .status(row.get("status", String.class) != null
                ? Enum.valueOf(AssetStatus.class, row.get("status", String.class))
                : null)
            .ownerId(row.get("owner_id", Long.class)).build())
        .one();
  }


  @Override
  public Mono<Asset> findById(Long assetId) {
    String sql = "SELECT * FROM telemetry.asset WHERE id = :id";

    return db.sql(sql).bind("id", assetId)
        .map((BiFunction<Row, RowMetadata, Asset>) (row, meta) -> Asset.builder()
            .id(row.get("id", Long.class)).name(row.get("name", String.class))
            .model(row.get("model", String.class))
            .serialNumber(row.get("serial_number", String.class))
            .typeId(row.get("type_id", Long.class))
            .status(row.get("status", String.class) != null
                ? Enum.valueOf(AssetStatus.class, row.get("status", String.class))
                : null)
            .ownerId(row.get("owner_id", Long.class)).build())
        .one();
  }



  @Override
  public Mono<Long> count(AssetFilter filter) {
    String sql = "SELECT COUNT(*) FROM telemetry.asset a "
        + "LEFT JOIN telemetry.asset_type t ON a.type_id = t.id "
        + "LEFT JOIN telemetry.asset_category c ON t.category_id = c.id " + "WHERE 1=1";

    if (filter.getCreatedBy() != null)
      sql += " AND a.created_by = :createdBy";
    if (filter.getModifiedBy() != null)
      sql += " AND a.modified_by = :modifiedBy";
    if (filter.getTypeId() != null)
      sql += " AND a.type_id = :typeId";
    if (filter.getOwnerId() != null)
      sql += " AND a.owner_id = :ownerId";
    if (filter.getCategory() != null)
      sql += " AND c.name = :category";

    var spec = db.sql(sql);

    if (filter.getCreatedBy() != null)
      spec = spec.bind("createdBy", filter.getCreatedBy());
    if (filter.getModifiedBy() != null)
      spec = spec.bind("modifiedBy", filter.getModifiedBy());
    if (filter.getTypeId() != null)
      spec = spec.bind("typeId", filter.getTypeId());
    if (filter.getOwnerId() != null)
      spec = spec.bind("ownerId", filter.getOwnerId());
    if (filter.getCategory() != null)
      spec = spec.bind("category", filter.getCategory());

    return spec.map(row -> row.get(0, Long.class)).one();
  }


  @Override
  public Flux<AssetDto> findAll(AssetFilter filter, int page, int size) {
    String sql = "SELECT a.*, t.type AS type_name, c.name AS category_name "
        + "FROM telemetry.asset a " + "LEFT JOIN telemetry.asset_type t ON a.type_id = t.id "
        + "LEFT JOIN telemetry.asset_category c ON t.category_id = c.id " + "WHERE 1=1";

    if (filter.getCreatedBy() != null)
      sql += " AND a.created_by = :createdBy";
    if (filter.getModifiedBy() != null)
      sql += " AND a.modified_by = :modifiedBy";
    if (filter.getTypeId() != null)
      sql += " AND a.type_id = :typeId";
    if (filter.getOwnerId() != null)
      sql += " AND a.owner_id = :ownerId";
    if (filter.getCategory() != null)
      sql += " AND c.name = :category";

    sql += " ORDER BY " + filter.getSortBy() + " " + filter.getSortDir();
    sql += " LIMIT " + size + " OFFSET " + (page * size);

    var spec = db.sql(sql);

    if (filter.getCreatedBy() != null)
      spec = spec.bind("createdBy", filter.getCreatedBy());
    if (filter.getModifiedBy() != null)
      spec = spec.bind("modifiedBy", filter.getModifiedBy());
    if (filter.getTypeId() != null)
      spec = spec.bind("typeId", filter.getTypeId());
    if (filter.getOwnerId() != null)
      spec = spec.bind("ownerId", filter.getOwnerId());
    if (filter.getCategory() != null)
      spec = spec.bind("category", filter.getCategory());

    return spec.map((row, meta) -> AssetDto.builder().assetId(row.get("id", Long.class))
        .name(row.get("name", String.class)).model(row.get("model", String.class))
        .serialNumber(row.get("serial_number", String.class)).typeId(row.get("type_id", Long.class))
        .assetType(row.get("type_name", String.class))
        .status(row.get("status", String.class) != null
            ? Enum.valueOf(AssetStatus.class, row.get("status", String.class))
            : null)
        .ownerId(row.get("owner_id", Long.class)).category(row.get("category_name", String.class))
        .build()).all();
  }


}
