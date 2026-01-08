package com.telemetry.engine.common.exception;

public class InvalidCodeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidCodeException(String message) {
    super(message);
  }

}
