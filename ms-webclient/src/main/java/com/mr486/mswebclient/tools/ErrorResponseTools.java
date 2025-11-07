package com.mr486.mswebclient.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ErrorResponseTools {

  public ErrorMessage getErrorMessage(String exceptionMessage, String microserviceName) {
    ErrorMessage errorMessage = new ErrorMessage();
    try {
      int firstBrace = exceptionMessage.indexOf('{');
      int lastBrace = exceptionMessage.lastIndexOf('}');
      String json = exceptionMessage.substring(firstBrace, lastBrace + 1);

      // Désérialiser
      ObjectMapper mapper = new ObjectMapper();
      ErrorResponse error = mapper.readValue(json, ErrorResponse.class);
      Integer status = error.getStatus();
      if (status >= 500) {
        errorMessage.setCritical(true);
      }
      errorMessage.setMessages(error.getMessages());
    } catch (Exception e) {
      errorMessage.setCritical(true);
      errorMessage.setMessages(List.of(microserviceName + " ne répond pas."));
    }
    return errorMessage;
  }
}
