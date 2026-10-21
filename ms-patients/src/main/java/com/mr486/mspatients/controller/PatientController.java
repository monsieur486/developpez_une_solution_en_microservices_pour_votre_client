package com.mr486.mspatients.controller;

import com.mr486.commun.dto.PatientDto;
import com.mr486.commun.dto.PatientForm;
import com.mr486.mspatients.mapper.PatientMapper;
import com.mr486.mspatients.service.PatientService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST des fiches patients, exposée derrière la gateway sous {@code /ms-patients/patients}.
 *
 * <p>Toutes les routes exigent l'authentification HTTP Basic. Les erreurs (400, 404, 409) sont
 * mises en forme par le gestionnaire d'erreurs commun de {@code ms-commun}.</p>
 *
 * <p><b>Exemple :</b> {@code GET /patients/1} avec des identifiants valides renvoie 200 et la fiche
 * du patient ; sans identifiants, 401.</p>
 */
@RestController
@RequestMapping(value = "/patients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final PatientMapper patientMapper;

    /**
     * Liste tous les patients.
     *
     * <p><b>Exemple :</b> {@code GET /patients} renvoie 200 et un tableau JSON, vide s'il n'y a
     * aucun patient.</p>
     *
     * @return 200 et la liste des patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDto>> getPatients() {
        return ResponseEntity.ok(patientMapper.versListeDto(patientService.findAll()));
    }

    /**
     * Renvoie la fiche d'un patient.
     *
     * <p><b>Exemple :</b> {@code GET /patients/1} renvoie 200 et la fiche ; {@code GET /patients/999}
     * renvoie 404 si ce patient n'existe pas.</p>
     *
     * @param id identifiant du patient
     * @return 200 et la fiche du patient
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientMapper.versDto(patientService.findById(id)));
    }

    /**
     * Crée un patient.
     *
     * <p><b>Exemple :</b> {@code POST /patients} avec un formulaire valide renvoie 201 et la fiche
     * créée ; sans nom de famille, 400 ; pour un patient déjà enregistré, 409.</p>
     *
     * @param patientForm données du patient, validées avant l'appel
     * @return 201 et la fiche créée, avec son identifiant
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientForm patientForm) {
        PatientDto cree = patientMapper.versDto(patientService.savePatient(patientForm));
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    /**
     * Met à jour la fiche d'un patient.
     *
     * <p><b>Exemple :</b> {@code PUT /patients/1} avec une nouvelle adresse renvoie 200 et la fiche
     * modifiée ; un identifiant inconnu renvoie 404.</p>
     *
     * @param id          identifiant du patient
     * @param patientForm nouvelles données, validées avant l'appel
     * @return 200 et la fiche mise à jour
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDto> update(
            @PathVariable Long id, @Valid @RequestBody PatientForm patientForm) {
        return ResponseEntity.ok(patientMapper.versDto(patientService.updatePatient(id, patientForm)));
    }
}
