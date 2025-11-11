package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Patient;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/app")
@Slf4j
public class PatientDetailController {

  private final RestTemplate restTemplate;
  private final String gatewayBase;
  private final ErrorResponseTools errorResponseTools;

  private final String microserviceName = "ms-patients";

  public PatientDetailController(RestTemplate restTemplate,
                                 @Value("${app.gateway.base-url}") String gatewayBase, ErrorResponseTools errorResponseTools) {
    this.restTemplate = restTemplate;
    this.gatewayBase = gatewayBase;
    this.errorResponseTools = errorResponseTools;
  }

  @GetMapping("/patients/{id}")
  public String patients(@PathVariable Long id, Model model) {
    try {
      ResponseEntity<Patient> response = restTemplate.exchange(
              gatewayBase + "/ms-patients/patients/" + id,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<>() {
              }
      );
      Patient patient = response.getBody();
      model.addAttribute("patient", patient);
    } catch (Exception ex) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), microserviceName);
      model.addAttribute("errorMessage", errorMessage);
    }
    return "patients/patient-detail";
  }

  @GetMapping("/patients/{id}/update")
  public String updatePatientForm(@PathVariable Long id, Model model) {
    try {
      ResponseEntity<Patient> response = restTemplate.exchange(
              gatewayBase + "/ms-patients/patients/" + id,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<>() {
              }
      );
      Patient patient = response.getBody();
      model.addAttribute("patient", patient);
      model.addAttribute("id", id);
    } catch (Exception ex) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), microserviceName);
      model.addAttribute("id", id);
      model.addAttribute("errorMessage", errorMessage);
    }
    return "patients/patient-update";
  }

  @PostMapping("/patients/{id}/update")
  public String updatePatient(@PathVariable Long id, @ModelAttribute PatientForm patient, Model model) {
    HttpEntity<PatientForm> requestEntity = new HttpEntity<>(patient);
    try {
      restTemplate.exchange(
              gatewayBase + "/ms-patients/patients/" + id,
              HttpMethod.PUT,
              requestEntity,
              new ParameterizedTypeReference<>() {
              }
      );
      return "redirect:/app/patients/" + id;
    } catch (Exception ex) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), microserviceName);
      model.addAttribute("patient", patient);
      model.addAttribute("id", id);
      model.addAttribute("errorMessage", errorMessage);
      return "patients/patient-update";
    }
  }

}
