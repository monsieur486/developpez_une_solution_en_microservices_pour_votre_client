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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    /** Nom du microservice cité dans les messages d'erreur. */
    private static final String MICROSERVICE = "ms-patients";

    private final PatientService patientService;
    private final ErrorResponseTools errorResponseTools;

    /**
     * Affiche la fiche détaillée d'un patient.
     *
     * <p><b>Exemple :</b> GET /app/patients/7 retourne la vue
     * "patients/patient-detail" avec le patient 7 dans le modèle.</p>
     *
     * @param id    identifiant du patient
     * @param model le modèle de la vue
     * @return le nom de la vue du détail du patient
     */
    @GetMapping("/patients/{id}")
    public String patientDetail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("patient", patientService.getPatientById(id));
        } catch (Exception ex) {
            log.warn("échec de récupération du patient {} : {}", id, ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "patients/patient-detail";
    }

    /**
     * Affiche le formulaire de modification d'un patient.
     *
     * <p><b>Exemple :</b> GET /app/patients/7/update retourne la vue
     * "patients/patient-update" pré-remplie avec les données du patient 7.</p>
     *
     * @param id    identifiant du patient à modifier
     * @param model le modèle de la vue
     * @return le nom de la vue du formulaire de modification
     */
    @GetMapping("/patients/{id}/update")
    public String updatePatientForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("patient", patientService.getPatientById(id));
            model.addAttribute("id", id);
        } catch (Exception ex) {
            log.warn("échec de récupération du patient {} : {}", id, ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("id", id);
            model.addAttribute("errorMessage", errorMessage);
        }
        return "patients/patient-update";
    }

    /**
     * Enregistre les modifications d'un patient.
     *
     * <p><b>Exemple :</b> POST /app/patients/7/update met à jour le patient 7
     * puis redirige vers sa fiche ; en cas d'échec, réaffiche le formulaire avec
     * le message d'erreur.</p>
     *
     * @param id      identifiant du patient à modifier
     * @param patient le formulaire portant les nouvelles données
     * @param model   le modèle de la vue
     * @return la redirection vers la fiche, ou le formulaire en cas d'erreur
     */
    @PostMapping("/patients/{id}/update")
    public String updatePatient(@PathVariable Long id, @ModelAttribute PatientForm patient, Model model) {
        try {
            patientService.updatePatient(id, patient);
            log.info("patient {} mis à jour", id);
            return "redirect:/app/patients/" + id;
        } catch (Exception ex) {
            log.warn("échec de mise à jour du patient {} : {}", id, ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("patient", patient);
            model.addAttribute("id", id);
            model.addAttribute("errorMessage", errorMessage);
            return "patients/patient-update";
        }
    }

}
