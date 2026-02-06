package com.telemetry.engine.common.exception;

public class InvalidRedirectUriException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidRedirectUriException(String message) {
    super(message);
  }

}
