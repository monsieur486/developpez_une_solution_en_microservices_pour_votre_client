package com.mr486.msnotes.configuration;

import com.mr486.msnotes.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the application.
 * Provides methods to handle specific and generic exceptions,
 * returning standardized HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @Value("${spring.application.name:application}")
  private String appName;

  /* ---------- Outils ---------- */

  private ResponseEntity<ErrorResponse> build(List<String> messages,
                                              HttpServletRequest req,
                                              HttpStatus status) {
    ErrorResponse body = ErrorResponse.builder()
            .timestamp(Instant.now().toString())
            .path(req.getRequestURI())
            .status(status.value())
            .errorCode(status.toString())
            .microserviceName(appName)
            .messages(messages)
            .build();
    return ResponseEntity.status(status).body(body);
  }

  /**
   * Handles MethodArgumentNotValidException.
   * Returns an HTTP 400 (Bad Request) response with validation error details.
   *
   * @param ex the thrown exception
   * @return ResponseEntity containing a map of field errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> messages = new ArrayList<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      messages.add(fe.getField() + ": " + fe.getDefaultMessage());
    }
    ex.getBindingResult().getGlobalErrors()
            .forEach(err -> messages.add(err.getObjectName() + ": " + err.getDefaultMessage()));

    return build(messages, request, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles 404 errors when no handler is found for a request.
   * Returns an HTTP 404 (Not Found) response with an error message.
   *
   * @param ex the thrown exception
   * @return ResponseEntity containing an error message
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest req) {
    return build(List.of(ex.getMessage()), req, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles generic exceptions (Exception).
   * Returns an HTTP 500 (Internal Server Error) response with an error message.
   *
   * @param ex the thrown exception
   * @return ResponseEntity containing an ApiResponse with error details
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest req) {
    return build(List.of(ex.getMessage()), req, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
