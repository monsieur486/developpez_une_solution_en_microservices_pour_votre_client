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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * Pages de la liste des patients et de l'ajout d'un patient.
 *
 * <p><b>Exemple :</b> GET /app/patients affiche la liste des patients ;
 * POST /app/patients/ajout crée un patient puis redirige vers la liste.</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    /**
     * Affiche une page de la liste des patients (20 par page), sans bloquer.
     *
     * <p><b>Exemple :</b> GET /app/patients retourne la vue "patients/patients"
     * avec la première page ; GET /app/patients?page=1 affiche la suivante.</p>
     *
     * @param page  numéro de la page demandée (à partir de 0)
     * @param model le modèle de la vue
     * @return un Mono émettant le nom de la vue de la liste des patients
     */
    @GetMapping("/patients")
    public Mono<String> patients(@RequestParam(defaultValue = "0") int page, Model model) {
        return patientService.getPatients(page)
                .map(pagePatients -> {
                    model.addAttribute("page", pagePatients);
                    return "patients/patients";
                })
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de récupération des patients : {}", ex.getMessage());
                    model.addAttribute("errorMessage", ex.getErrorMessage());
                    return Mono.just("patients/patients");
                });
    }

    /**
     * Affiche le formulaire d'ajout d'un patient.
     *
     * <p><b>Exemple :</b> GET /app/patients/ajout retourne la vue
     * "patients/patient-ajout" avec un formulaire vierge.</p>
     *
     * @param model le modèle de la vue
     * @return le nom de la vue du formulaire d'ajout
     */
    @GetMapping("/patients/ajout")
    public String showCreatePatientForm(Model model) {
        model.addAttribute("patient", new PatientForm());
        return "patients/patient-ajout";
    }

    /**
     * Crée un patient à partir du formulaire soumis, sans bloquer.
     *
     * <p><b>Exemple :</b> POST /app/patients/ajout crée le patient puis redirige
     * vers la liste ; en cas d'échec, réaffiche le formulaire avec le message
     * d'erreur.</p>
     *
     * @param patient le formulaire du patient à créer
     * @param model   le modèle de la vue
     * @return un Mono émettant la redirection, ou le formulaire en cas d'erreur
     */
    @PostMapping("/patients/ajout")
    public Mono<String> ajoutPatientPost(@ModelAttribute PatientForm patient, Model model) {
        return patientService.createPatient(patient)
                .doOnSuccess(v -> log.info("patient créé : {} {}", patient.getFirstName(), patient.getLastName()))
                .thenReturn("redirect:/app/patients")
                .onErrorResume(GatewayException.class, ex -> {
                    log.warn("échec de création d'un patient : {}", ex.getMessage());
                    model.addAttribute("patient", patient);
                    model.addAttribute("errorMessage", ex.getErrorMessage());
                    return Mono.just("patients/patient-ajout");
                });
    }
}
