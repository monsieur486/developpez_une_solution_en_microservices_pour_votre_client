package com.mr486.msrisque.controller;

import com.mr486.msrisque.service.EvaluationService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(title = "Gestion du risque de diabète d'un patient API", version = "v1"))
@SecurityRequirement(name = "basicAuth")
public class EvaluationController {

  private final EvaluationService evaluationService;

  @Tag(name = "Évalue le risque de diabète d'un patient par son ID")
  @GetMapping(value = "/evaluation/{patientId}")
  public String evaluate(@PathVariable Long patientId) {
    return evaluationService.evaluationDuRisque(patientId);
  }
}
