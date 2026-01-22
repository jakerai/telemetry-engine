package com.telemetry.engine.auth.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.telemetry.engine.common.context.RequestContext;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.exception.DataPersistenceException;
import com.telemetry.engine.common.exception.DuplicateResourceException;
import com.telemetry.engine.common.exception.FileUploadException;
import com.telemetry.engine.common.exception.InvalidCodeException;
import com.telemetry.engine.common.exception.InvalidTokenException;
import com.telemetry.engine.common.exception.JwtKeyStoreException;
import com.telemetry.engine.common.exception.NotFoundException;
import com.telemetry.engine.common.exception.TokenGenerationException;
import com.telemetry.engine.common.exception.TooManyAttemptsException;
import com.telemetry.engine.common.exception.UnauthorizedException;


/**
 * @author Vishal Rai
 */

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ServiceResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ServiceResponse.error("Unauthorized", HttpStatus.UNAUTHORIZED.value()));
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ServiceResponse<Void>> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
      return ResponseEntity
              .status(HttpStatus.FORBIDDEN)
              .body(ServiceResponse.error("Forbidden", HttpStatus.FORBIDDEN.value()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ServiceResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
      Map<String, String> errors = new HashMap<>();
      ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

      ServiceResponse<Map<String, String>> response = ServiceResponse.<Map<String, String>>builder()
              .success(false)
              .status(HttpStatus.BAD_REQUEST.value())
              .message("Validation Failed")
              .errors(errors)
              .timestamp(Instant.now())
              .requestId(RequestContext.getTraceId())
              .build();

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ServiceResponse<Void>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
      StringBuilder supportedTypes = new StringBuilder();
      ex.getSupportedMediaTypes().forEach(t -> supportedTypes.append(t.toString()).append(", "));
      String supported = supportedTypes.length() > 0
              ? supportedTypes.substring(0, supportedTypes.length() - 2)
              : "None";

      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(ServiceResponse.error("Unsupported Content-Type. Supported types: " + supported,
                      HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ServiceResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ServiceResponse.error("The requested resource was not found", HttpStatus.NOT_FOUND.value()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ServiceResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(ServiceResponse.error("Malformed request: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<ServiceResponse<Void>> handleFileUploadException(FileUploadException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ServiceResponse<Void>> handleMissingRequestPart(MissingServletRequestPartException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ServiceResponse<Void>> handleNotFoundException(NotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ServiceResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value()));
  }

  @ExceptionHandler(DataPersistenceException.class)
  public ResponseEntity<ServiceResponse<Void>> handleDataPersistenceException(DataPersistenceException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @ExceptionHandler(TokenGenerationException.class)
  public ResponseEntity<ServiceResponse<Void>> handleTokenGenerationException(TokenGenerationException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ServiceResponse<Void>> handleInvalidTokenException(InvalidTokenException ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
  }

  @ExceptionHandler(TooManyAttemptsException.class)
  public ResponseEntity<ServiceResponse<Void>> handleTooManyAttemptsException(TooManyAttemptsException ex) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value()));
  }

  @ExceptionHandler(JwtKeyStoreException.class)
  public ResponseEntity<ServiceResponse<Void>> handleJwtKeyStoreException(JwtKeyStoreException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @ExceptionHandler(InvalidCodeException.class)
  public ResponseEntity<ServiceResponse<Void>> handleInvalidCodeException(InvalidCodeException ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(ServiceResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
  }

}
