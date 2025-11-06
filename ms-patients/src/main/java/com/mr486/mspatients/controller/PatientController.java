package com.mr486.mspatients.controller;

import com.mr486.mspatients.dto.PatientForm;
import com.mr486.mspatients.model.Patient;
import com.mr486.mspatients.service.PatientService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(title = "Gestion des patients API", version = "v1"))
@SecurityRequirement(name = "basicAuth")
public class PatientController {

  private final PatientService patientService;

  @Tag(name = "Récupère touts les patients")
  @GetMapping(value = "/patients", produces = "application/json")
  public ResponseEntity<List<Patient>> getPatients() {
    return ResponseEntity.ok(patientService.findAll());
  }

  @Tag(name = "Récupère un patient par son ID")
  @GetMapping(value = "/patients/{id}", produces = "application/json")
  public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
    Patient patient = patientService.findById(id);
    return ResponseEntity.ok(patient);
  }

  @Tag(name = "Crée un nouveau patient")
  @PostMapping(value = "/patients", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Patient> createPatient(@Valid @RequestBody PatientForm patientForm) {
    Patient savedPatient = patientService.savePatient(patientForm);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
  }

  @Tag(name = "Met à jour un patient existant")
  @PutMapping(value = "/patients/{id}", produces = "application/json")
  public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody PatientForm form) {
    Patient updated = patientService.updatePatient(id, form);
    return ResponseEntity.ok(updated);
  }
}
