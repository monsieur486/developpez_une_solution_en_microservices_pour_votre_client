package com.mr486.mswebclient.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.mswebclient.dto.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ErrorResponseTools {

  public ErrorMessage getErrorMessage(String exceptionMessage, String microserviceName) {
    try {
      int firstBrace = exceptionMessage.indexOf('{');
      int lastBrace = exceptionMessage.lastIndexOf('}');
      String json = exceptionMessage.substring(firstBrace, lastBrace + 1);

      // Désérialiser
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(json, ErrorMessage.class);
    } catch (Exception e) {
      ErrorMessage errorMessage = new ErrorMessage();
      errorMessage.setStatus(503);
      errorMessage.setMessage(microserviceName + " ne répond pas.");
      return errorMessage;
    }
  }
}
