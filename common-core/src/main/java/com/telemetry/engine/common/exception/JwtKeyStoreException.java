package com.telemetry.engine.common.exception;

public class JwtKeyStoreException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public JwtKeyStoreException(String message) {
    super(message);
  }
}
