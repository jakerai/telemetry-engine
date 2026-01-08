package com.telemetry.engine.common.exception;

public class TooManyAttemptsException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public TooManyAttemptsException(String message) {
    super(message);
  }
}
