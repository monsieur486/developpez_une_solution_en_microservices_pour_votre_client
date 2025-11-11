package com.mr486.mswebclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {
  Integer status;
  String message;

  public Boolean critical() {
    return this.status != null && this.status >= 500;
  }
}
