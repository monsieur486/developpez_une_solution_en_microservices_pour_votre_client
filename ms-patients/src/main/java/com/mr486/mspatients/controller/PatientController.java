package com.mr486.mspatients.controller;

import com.mr486.mspatients.dto.PageReponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose l'API REST de gestion des patients (CRUD partiel : lecture, création,
 * mise à jour).
 *
 * <p><b>Exemple :</b> GET /patients/7 retourne le patient 7 ; POST /patients crée
 * un patient à partir du corps JSON validé.</p>
 */
@RestController
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(title = "Gestion des patients API", version = "v1"))
@SecurityRequirement(name = "basicAuth")
public class PatientController {

    private final PatientService patientService;

    /**
     * Retourne une page de patients (20 par page, triés par identifiant).
     *
     * <p><b>Exemple :</b> GET /patients?page=0 retourne une réponse 200 avec les
     * 20 premiers patients et les informations de navigation.</p>
     *
     * @param page numéro de la page demandée (à partir de 0)
     * @return la réponse HTTP contenant la page de patients
     */
    @Tag(name = "Récupère les patients par page")
    @GetMapping(value = "/patients", produces = "application/json")
    public ResponseEntity<PageReponse<Patient>> getPatients(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(PageReponse.depuis(patientService.findAll(page)));
    }

    /**
     * Retourne un patient par son identifiant.
     *
     * <p><b>Exemple :</b> getPatient(7L) retourne une réponse 200 avec le patient
     * 7 ; un id inexistant donne une réponse 404.</p>
     *
     * @param id identifiant du patient
     * @return la réponse HTTP contenant le patient
     */
    @Tag(name = "Récupère un patient par son ID")
    @GetMapping(value = "/patients/{id}", produces = "application/json")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Crée un nouveau patient.
     *
     * <p><b>Exemple :</b> createPatient(form) retourne une réponse 201 avec le
     * patient créé ; un formulaire invalide donne une réponse 400.</p>
     *
     * @param patientForm les données du patient à créer
     * @return la réponse HTTP 201 contenant le patient créé
     */
    @Tag(name = "Crée un nouveau patient")
    @PostMapping(value = "/patients", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody PatientForm patientForm) {
        Patient savedPatient = patientService.savePatient(patientForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
    }

    /**
     * Met à jour un patient existant.
     *
     * <p><b>Exemple :</b> update(7L, form) retourne une réponse 200 avec le
     * patient mis à jour ; un id inexistant donne une réponse 404.</p>
     *
     * @param id   identifiant du patient à mettre à jour
     * @param form les nouvelles données du patient
     * @return la réponse HTTP contenant le patient mis à jour
     */
    @Tag(name = "Met à jour un patient existant")
    @PutMapping(value = "/patients/{id}", produces = "application/json")
    public ResponseEntity<Patient> update(@PathVariable Long id, @Valid @RequestBody PatientForm form) {
        Patient updated = patientService.updatePatient(id, form);
        return ResponseEntity.ok(updated);
    }
}
