-- =======================================================
-- Telemetry Database Initialization Script
-- =======================================================
-- This script sets up the telemetry schema and tables for:
-- 1. Asset metadata (category, type, asset)
-- 2. Latest asset locations
-- 3. Historical asset location events (time-series)
--
-- Features:
-- - Dedicated schema: telemetry
-- - Uses TimescaleDB for time-series
-- - Uses PostGIS for geospatial data (GEOGRAPHY(Point,4326))
-- - asset_current_location: upsert-ready using asset_id as UNIQUE PRIMARY KEY
-- - asset_location_history: hypertable, chunked daily, with compression
-- - Indexes for fast lookups and GIS queries
-- - modified_at triggers for audit
-- - Fully compatible with reactive R2DBC + Kafka consumer + upsert design
--
-- IMPORTANT:
-- Run with a fresh volume or clean database to avoid extension/type issues
-- =======================================================

-- =======================================================
-- 0. Create dedicated schema
-- =======================================================
CREATE SCHEMA IF NOT EXISTS telemetry AUTHORIZATION admin;

-- =======================================================
-- 1. Enable Extensions
-- =======================================================
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;
CREATE EXTENSION IF NOT EXISTS postgis CASCADE;

-- =======================================================
-- 2. Asset Category
-- =======================================================
CREATE TABLE IF NOT EXISTS telemetry.asset_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_by BIGINT,
    modified_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT now(),
    modified_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_asset_category_name
    ON telemetry.asset_category(name);
-- =======================================================
-- 3. Asset Type
-- =======================================================
CREATE TABLE IF NOT EXISTS telemetry.asset_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL REFERENCES telemetry.asset_category(id),
    description TEXT,
    created_by BIGINT,
    modified_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT now(),
    modified_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_asset_type_category_id
    ON telemetry.asset_type(category_id);
CREATE INDEX IF NOT EXISTS idx_asset_type_name
    ON telemetry.asset_type(name);
-- =======================================================
-- 4. Asset
-- =======================================================
CREATE TABLE IF NOT EXISTS telemetry.asset (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    type_id BIGINT NOT NULL REFERENCES telemetry.asset_type(id),
    serial_number VARCHAR(100),
    status VARCHAR(50),
    owner_id BIGINT,
    created_by BIGINT,
    modified_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT now(),
    modified_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_asset_name ON telemetry.asset(name);
CREATE INDEX IF NOT EXISTS idx_asset_model ON telemetry.asset(model);
CREATE INDEX IF NOT EXISTS idx_asset_type_id ON telemetry.asset(type_id);
CREATE INDEX IF NOT EXISTS idx_asset_serial_number ON telemetry.asset(serial_number);
CREATE INDEX IF NOT EXISTS idx_asset_owner_id_created_at_desc ON telemetry.asset (owner_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_asset_created_by_created_at_desc ON telemetry.asset (created_by, created_at DESC);



-- =======================================================
-- 5. Asset Current Location
-- =======================================================
CREATE TABLE IF NOT EXISTS telemetry.asset_current_location (
    asset_id BIGINT PRIMARY KEY REFERENCES telemetry.asset(id) ON DELETE CASCADE,
    device_ts TIMESTAMPTZ NOT NULL,
    operator_id BIGINT NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    h3_index BIGINT,
    speed DOUBLE PRECISION,
    heading DOUBLE PRECISION,
    processed_at TIMESTAMPTZ,
    created_by BIGINT,
    modified_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT now(),
    modified_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_asset_current_location_geo ON telemetry.asset_current_location USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_asset_current_location_operator_id ON telemetry.asset_current_location(operator_id);
CREATE INDEX IF NOT EXISTS idx_asset_current_location_h3_index ON telemetry.asset_current_location(h3_index);
CREATE INDEX IF NOT EXISTS idx_asset_current_location_device_ts ON telemetry.asset_current_location(device_ts);

-- =======================================================
-- 6. Asset Location Event (time-series)
-- =======================================================
CREATE TABLE IF NOT EXISTS telemetry.asset_location_history (
    asset_id BIGINT NOT NULL REFERENCES telemetry.asset(id) ON DELETE CASCADE,
    device_ts TIMESTAMPTZ NOT NULL,
    operator_id BIGINT NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    h3_index BIGINT,
    speed DOUBLE PRECISION,
    heading DOUBLE PRECISION,
    processed_at TIMESTAMPTZ
);

-- Convert to hypertable (TimescaleDB)
SELECT create_hypertable(
    'telemetry.asset_location_history',
    'device_ts',
    chunk_time_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_asset_location_history_asset_id ON telemetry.asset_location_history(asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_location_history_device_ts ON telemetry.asset_location_history(device_ts DESC);
CREATE INDEX IF NOT EXISTS idx_asset_location_history_geo ON telemetry.asset_location_history USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_asset_location_history_h3_index ON telemetry.asset_location_history(h3_index);
-- Compression policy for events older than 7 days
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM timescaledb_information.compression_settings
        WHERE hypertable_name = 'asset_location_history'
    ) THEN
        ALTER TABLE telemetry.asset_location_history SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'asset_id',
            timescaledb.compress_orderby = 'device_ts DESC'
        );
        PERFORM add_compression_policy('telemetry.asset_location_history', INTERVAL '7 days');
    END IF;
END $$;

-- =======================================================
-- 7. Trigger for modified_at
-- =======================================================
CREATE OR REPLACE FUNCTION telemetry.trg_set_modified_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach triggers
CREATE TRIGGER trg_asset_category_modified_at
BEFORE UPDATE ON telemetry.asset_category
FOR EACH ROW EXECUTE FUNCTION telemetry.trg_set_modified_at();

CREATE TRIGGER trg_asset_type_modified_at
BEFORE UPDATE ON telemetry.asset_type
FOR EACH ROW EXECUTE FUNCTION telemetry.trg_set_modified_at();

CREATE TRIGGER trg_asset_modified_at
BEFORE UPDATE ON telemetry.asset
FOR EACH ROW EXECUTE FUNCTION telemetry.trg_set_modified_at();

CREATE TRIGGER trg_asset_current_location_modified_at
BEFORE UPDATE ON telemetry.asset_current_location
FOR EACH ROW EXECUTE FUNCTION telemetry.trg_set_modified_at();
