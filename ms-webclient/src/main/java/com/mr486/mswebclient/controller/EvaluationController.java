package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Risque;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/app")
public class EvaluationController {

  private final RestTemplate restTemplate;
  private final String gatewayBase;
  private final ErrorResponseTools errorResponseTools;

  private final String microserviceName = "ms-risque";

  public EvaluationController(RestTemplate restTemplate,
                              @Value("${app.gateway.base-url}") String gatewayBase, ErrorResponseTools errorResponseTools) {
    this.restTemplate = restTemplate;
    this.gatewayBase = gatewayBase;
    this.errorResponseTools = errorResponseTools;
  }

  @GetMapping("/patients/{patientId}/evaluation")
  public String getEvaluation(Model model, @PathVariable Long patientId) {
    Risque evaluation = new Risque();
    try {
      ResponseEntity<Risque> response = restTemplate.exchange(
              gatewayBase + "/ms-risque/patients/" + patientId + "/evaluation",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<>() {
              }
      );
      evaluation = response.getBody();
      model.addAttribute("evaluation", evaluation.getLevel());
      model.addAttribute("patientId", patientId);
    } catch (Exception ex) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), microserviceName);
      model.addAttribute("errorMessage", errorMessage);
      model.addAttribute("patientId", patientId);
    }
    return "evaluation/evaluation";
  }
}
