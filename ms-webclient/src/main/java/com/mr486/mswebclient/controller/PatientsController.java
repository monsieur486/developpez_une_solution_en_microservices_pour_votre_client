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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
@RequestMapping("/app")
@Slf4j
public class PatientsController {

  private final RestTemplate restTemplate;
  private final String gatewayBase;
  private final ErrorResponseTools errorResponseTools;

  private final String microserviceName = "ms-patients";

  public PatientsController(RestTemplate restTemplate,
                            @Value("${app.gateway.base-url}") String gatewayBase, ErrorResponseTools errorResponseTools) {
    this.restTemplate = restTemplate;
    this.gatewayBase = gatewayBase;
    this.errorResponseTools = errorResponseTools;
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    List<Patient> patients = List.of();
    try {
      ResponseEntity<List<Patient>> response = restTemplate.exchange(
              gatewayBase + "/ms-patients/patients",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<>() {
              }
      );
      patients = response.getBody();
      model.addAttribute("patients", patients);
    } catch (Exception ex) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), microserviceName);
      model.addAttribute("errorMessage", errorMessage);
    }
    return "patients";
  }

  @GetMapping("/dashboard/ajout")
  public String showCreatePatientForm(Model model) {
    model.addAttribute("patient", new PatientForm());
    return "patient-ajout";
  }

  @PostMapping("/dashboard/ajout")
  public String ajoutPatientPost(@ModelAttribute PatientForm patient, Model model) {
    HttpEntity<PatientForm> requestEntity = new HttpEntity<>(patient);
    try {
      restTemplate.exchange(
              gatewayBase + "/ms-patients/patients",
              HttpMethod.POST,
              requestEntity,
              new ParameterizedTypeReference<>() {
              }
      );
      return "redirect:/app/dashboard";
    } catch (Exception ex) {
      ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), microserviceName);
      model.addAttribute("patient", patient);
      model.addAttribute("errorMessage", errorMessage);
      return "patient-ajout";
    }
  }
}
