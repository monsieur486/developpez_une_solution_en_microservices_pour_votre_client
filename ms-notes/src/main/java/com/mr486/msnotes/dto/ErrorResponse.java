package com.mr486.msnotes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
  String timestamp;
  String microserviceName;
  String path;
  Integer status;
  String errorCode;
  List<String> messages;
}
