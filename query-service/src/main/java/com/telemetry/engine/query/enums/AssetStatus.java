package com.telemetry.engine.query.enums;


public enum AssetStatus {
  ACTIVE, // Asset is live and reporting telemetry
  INACTIVE, // Temporarily disabled or not reporting
  DELETED, // Soft-deleted
  SUSPENDED, // Optional: revoked, blocked for policy/security reasons
  MAINTENANCE, // Optional: under maintenance, no telemetry ingestion
  UNKNOWN
}
