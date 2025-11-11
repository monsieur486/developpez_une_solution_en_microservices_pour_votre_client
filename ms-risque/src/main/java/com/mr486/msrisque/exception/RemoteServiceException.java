package com.mr486.msrisque.exception;

import com.mr486.msrisque.dto.ErrorResponse;
import lombok.Getter;

@Getter
public abstract class RemoteServiceException extends RuntimeException {
  private final ErrorResponse error;
  private final int httpStatus;

  protected RemoteServiceException(String message, int httpStatus, ErrorResponse error) {
    super(message);
    this.httpStatus = httpStatus;
    this.error = error;
  }
}








