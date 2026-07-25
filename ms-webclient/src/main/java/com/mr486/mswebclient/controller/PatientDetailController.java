package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * Pages du détail d'un patient et de sa modification.
 *
 * <p><b>Exemple :</b> GET /app/patients/7 affiche la fiche du patient 7 ;
 * POST /app/patients/7/update enregistre ses nouvelles données.</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class PatientDetailController {

    private final PatientService patientService;

    /**
     * Affiche la fiche détaillée d'un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> GET /app/patients/7 retourne la vue
     * "patients/patient-detail" avec le patient 7 dans le modèle.</p>
     *
     * @param id    identifiant du patient
     * @param model le modèle de la vue
     * @return un Mono émettant le nom de la vue du détail du patient
     */
    @GetMapping("/patients/{id}")
    public Mono<String> patientDetail(@PathVariable Long id, Model model) {
        return patientService.getPatientById(id)
                .map(patient -> {
                    model.addAttribute("patient", patient);
                    return "patients/patient-detail";
                })
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de récupération du patient {} : {}", id, ex.getMessage());
                    model.addAttribute("errorMessage", ex.getErrorMessage());
                    return Mono.just("patients/patient-detail");
                });
    }

    /**
     * Affiche le formulaire de modification d'un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> GET /app/patients/7/update retourne la vue
     * "patients/patient-update" pré-remplie avec les données du patient 7.</p>
     *
     * @param id    identifiant du patient à modifier
     * @param model le modèle de la vue
     * @return un Mono émettant le nom de la vue du formulaire de modification
     */
    @GetMapping("/patients/{id}/update")
    public Mono<String> updatePatientForm(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return patientService.getPatientById(id)
                .map(patient -> {
                    model.addAttribute("patient", patient);
                    return "patients/patient-update";
                })
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de récupération du patient {} : {}", id, ex.getMessage());
                    model.addAttribute("errorMessage", ex.getErrorMessage());
                    return Mono.just("patients/patient-update");
                });
    }

    /**
     * Enregistre les modifications d'un patient, sans bloquer.
     *
     * <p><b>Exemple :</b> POST /app/patients/7/update met à jour le patient 7
     * puis redirige vers sa fiche ; en cas d'échec, réaffiche le formulaire avec
     * le message d'erreur.</p>
     *
     * @param id      identifiant du patient à modifier
     * @param patient le formulaire portant les nouvelles données
     * @param model   le modèle de la vue
     * @return un Mono émettant la redirection, ou le formulaire en cas d'erreur
     */
    @PostMapping("/patients/{id}/update")
    public Mono<String> updatePatient(@PathVariable Long id, @ModelAttribute PatientForm patient, Model model) {
        return patientService.updatePatient(id, patient)
                .doOnSuccess(v -> log.info("patient {} mis à jour", id))
                .thenReturn("redirect:/app/patients/" + id)
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de mise à jour du patient {} : {}", id, ex.getMessage());
                    model.addAttribute("patient", patient);
                    model.addAttribute("id", id);
                    model.addAttribute("errorMessage", ex.getErrorMessage());
                    return Mono.just("patients/patient-update");
                });
    }

}
