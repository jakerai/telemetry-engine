package com.telemetry.engine.common.exception;

/**
 * @author Vishal Rai
 */

public class UnauthorizedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UnauthorizedException(String message) {
    super(message);
  }

}
