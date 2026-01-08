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
import com.telemetry.engine.common.dto.response.ResponseStatus;
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
public class GlobalExceptionHandlerController {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
    ResponseStatus status = ResponseStatus.builder().success(false).message("Unauthorized")
        .status(HttpStatus.UNAUTHORIZED.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<Object> handleAuthorizationDeniedException(
      AuthorizationDeniedException ex) {
    ResponseStatus status = ResponseStatus.builder().success(false).message("Forbidden")
        .status(HttpStatus.FORBIDDEN.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });

    ResponseStatus status = ResponseStatus.builder().success(false).message("Validation Failed")
        .status(HttpStatus.BAD_REQUEST.value()).errors(errors).timestamp(Instant.now()).build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex) {

    StringBuilder supportedTypes = new StringBuilder();
    ex.getSupportedMediaTypes().forEach(t -> supportedTypes.append(t.toString()).append(", "));
    String supported =
        supportedTypes.length() > 0 ? supportedTypes.substring(0, supportedTypes.length() - 2)
            : "None";

    ResponseStatus status = ResponseStatus.builder().success(false).message("Validation Failed")
        .status(HttpStatus.BAD_REQUEST.value())
        .errors("Unsupported Content-Type. Supported types: " + supported).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Resource Not Found")
        .status(HttpStatus.NOT_FOUND.value()).errors("The requested resource was not found")
        .timestamp(Instant.now()).build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }


  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ResponseStatus> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Validation Failed")
        .status(HttpStatus.BAD_REQUEST.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<ResponseStatus> handleFileUploadException(FileUploadException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Validation Failed")
        .status(HttpStatus.BAD_REQUEST.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<?> handleMissingServletRequestPartException(
      MissingServletRequestPartException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Validation Failed")
        .status(HttpStatus.BAD_REQUEST.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }


  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Not Found")
        .status(HttpStatus.BAD_REQUEST.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<?> handleDuplicateResourceException(DuplicateResourceException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Duplicate Resource")
        .status(HttpStatus.CONFLICT.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(DataPersistenceException.class)
  public ResponseEntity<?> handleDataPersistenceException(DataPersistenceException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Persistence Error")
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).errors(ex.getMessage())
        .timestamp(Instant.now()).build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(TokenGenerationException.class)
  public ResponseEntity<?> handleTokenGenerationException(TokenGenerationException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Token Error")
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).errors(ex.getMessage())
        .timestamp(Instant.now()).build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<?> handleInvalidTokenException(InvalidTokenException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Invalid Token Error")
        .status(HttpStatus.UNAUTHORIZED.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }


  @ExceptionHandler(TooManyAttemptsException.class)
  public ResponseEntity<?> handleTooManyAttemptsException(TooManyAttemptsException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Too Many Attemps")
        .status(HttpStatus.TOO_MANY_REQUESTS.value()).errors(ex.getMessage())
        .timestamp(Instant.now()).build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(JwtKeyStoreException.class)
  public ResponseEntity<?> handleJsonProcessingException(JwtKeyStoreException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Json Processing Error")
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value()).errors(ex.getMessage())
        .timestamp(Instant.now()).build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }

  @ExceptionHandler(InvalidCodeException.class)
  public ResponseEntity<?> handleInvalidCodeException(InvalidCodeException ex) {

    ResponseStatus status = ResponseStatus.builder().success(false).message("Invalid Code Error")
        .status(HttpStatus.UNAUTHORIZED.value()).errors(ex.getMessage()).timestamp(Instant.now())
        .build();

    return ResponseEntity.status(status.getStatus()).body(status);
  }


}
