package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.PatientForm;
import com.mr486.mswebclient.service.PatientService;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    /** Nom du microservice cité dans les messages d'erreur. */
    private static final String MICROSERVICE = "ms-patients";

    private final PatientService patientService;
    private final ErrorResponseTools errorResponseTools;

    /**
     * Affiche la liste des patients.
     *
     * <p><b>Exemple :</b> GET /app/patients retourne la vue "patients/patients"
     * avec la liste des patients dans le modèle.</p>
     *
     * @param model le modèle de la vue
     * @return le nom de la vue de la liste des patients
     */
    @GetMapping("/patients")
    public String patients(Model model) {
        try {
            model.addAttribute("patients", patientService.getPatients());
        } catch (Exception ex) {
            log.warn("échec de récupération des patients : {}", ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "patients/patients";
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
     * Crée un patient à partir du formulaire soumis.
     *
     * <p><b>Exemple :</b> POST /app/patients/ajout crée le patient puis redirige
     * vers la liste ; en cas d'échec, réaffiche le formulaire avec le message
     * d'erreur.</p>
     *
     * @param patient le formulaire du patient à créer
     * @param model   le modèle de la vue
     * @return la redirection vers la liste, ou le formulaire en cas d'erreur
     */
    @PostMapping("/patients/ajout")
    public String ajoutPatientPost(@ModelAttribute PatientForm patient, Model model) {
        try {
            patientService.createPatient(patient);
            log.info("patient créé : {} {}", patient.getFirstName(), patient.getLastName());
            return "redirect:/app/patients";
        } catch (Exception ex) {
            log.warn("échec de création d'un patient : {}", ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("patient", patient);
            model.addAttribute("errorMessage", errorMessage);
            return "patients/patient-ajout";
        }
    }
}
