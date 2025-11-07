package com.mr486.mswebclient.controller;

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

  public EvaluationController(RestTemplate restTemplate,
                              @Value("${app.gateway.base-url}") String gatewayBase) {
    this.restTemplate = restTemplate;
    this.gatewayBase = gatewayBase;
  }

  @GetMapping("/dashboard/{patientId}/evaluation")
  public String getNotes(Model model, @PathVariable Long patientId) {
    String evaluation;
    ResponseEntity<String> response = restTemplate.exchange(
            gatewayBase + "/ms-risque/evaluation/" + patientId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {
            }
    );
    evaluation = response.getBody();
    model.addAttribute("evaluation", evaluation);
    model.addAttribute("patientId", patientId);
    return "evaluation";
  }
}
