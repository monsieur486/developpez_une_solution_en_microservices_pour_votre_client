package com.mr486.msrisque.configuration;

import com.mr486.msrisque.dto.ErrorResponse;
import com.mr486.msrisque.exception.RemoteServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RemoteServiceException.class)
  public ResponseEntity<ErrorResponse> handleRemote(RemoteServiceException ex) {
    return build(ex.getMessage(), HttpStatus.valueOf(ex.getHttpStatus()));
  }

  /**
   * Handles 404 errors when no handler is found for a request.
   * Returns an HTTP 404 (Not Found) response with an error message.
   *
   * @param ex the thrown exception
   * @return ResponseEntity containing an error message
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
    return build(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  /**
   * Handles generic exceptions (Exception).
   * Returns an HTTP 500 (Internal Server Error) response with an error message.
   *
   * @param ex the thrown exception
   * @return ResponseEntity containing an ApiResponse with error details
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    return build(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ErrorResponse> build(String message, HttpStatus status) {
    ErrorResponse body = ErrorResponse.builder()
            .status(status.value())
            .message(message)
            .build();
    return ResponseEntity.status(status).body(body);
  }
}

