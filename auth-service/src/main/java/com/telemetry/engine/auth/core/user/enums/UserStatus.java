package com.telemetry.engine.auth.core.user.enums;

public enum UserStatus {
  ACTIVE, // Normal, active user
  LOCKED, // Temporarily locked (too many login attempts)
  DELETED, // Account has been deleted
  INACTIVE, // User registered but never activated (email not verified)
  SUSPENDED, // Temporarily disabled by admin
  PENDING_VERIFICATION, // Waiting for email / phone /kyc verification
  ARCHIVED, // Inactive for a long time, archived
  BANNED // Permanently banned
}
