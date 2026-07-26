package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.EvaluationService;
import com.mr486.mswebclient.service.NoteService;
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
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * Fiche patient : informations du patient, niveau de risque et notes médicales
 * paginées sur une même page, et modification du patient.
 *
 * <p><b>Exemple :</b> GET /app/patients/3 affiche la fiche du patient 3 avec son
 * risque et ses 5 dernières notes ; GET /app/patients/3?notesPage=1 affiche la
 * page suivante des notes.</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class PatientDetailController {

    private final PatientService patientService;
    private final NoteService noteService;
    private final EvaluationService evaluationService;

    /**
     * Affiche la fiche complète d'un patient, sans bloquer : les informations du
     * patient, son niveau de risque et une page de ses notes sont récupérés en
     * parallèle. Si ms-risque ou ms-notes ne répond pas, la fiche s'affiche
     * quand même avec un message d'erreur dans la section concernée.
     *
     * <p><b>Exemple :</b> GET /app/patients/3 retourne la vue
     * "patients/patient-detail" avec le patient, {@code "In Danger"} et les 5
     * dernières notes ; si ms-notes est en panne, la section notes affiche
     * « ms-notes ne répond pas. ».</p>
     *
     * @param id        identifiant du patient
     * @param notesPage numéro de la page de notes demandée (à partir de 0)
     * @param model     le modèle de la vue
     * @return un Mono émettant le nom de la vue de la fiche patient
     */
    @GetMapping("/patients/{id}")
    public Mono<String> patientDetail(@PathVariable Long id,
                                      @RequestParam(defaultValue = "0") int notesPage, Model model) {
        return patientService.getPatientById(id)
                .flatMap(patient -> {
                    model.addAttribute("patient", patient);
                    return Mono.zip(chargeEvaluation(id, model), chargeNotes(id, notesPage, model));
                })
                .thenReturn("patients/patient-detail")
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

    // Charge le niveau de risque ; en cas de panne, pose un message d'erreur de section.
    private Mono<Boolean> chargeEvaluation(Long id, Model model) {
        return evaluationService.getEvaluationByPatientId(id)
                .map(risque -> {
                    model.addAttribute("evaluation", risque.getLevel());
                    return true;
                })
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de l'évaluation du patient {} : {}", id, ex.getMessage());
                    model.addAttribute("erreurEvaluation", ex.getErrorMessage());
                    return Mono.just(false);
                });
    }

    // Charge une page de notes ; en cas de panne, pose un message d'erreur de section.
    private Mono<Boolean> chargeNotes(Long id, int notesPage, Model model) {
        return noteService.getNotesPagines(id, notesPage)
                .map(notes -> {
                    model.addAttribute("notesPage", notes);
                    return true;
                })
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de récupération des notes du patient {} : {}", id, ex.getMessage());
                    model.addAttribute("erreurNotes", ex.getErrorMessage());
                    return Mono.just(false);
                });
    }

}
