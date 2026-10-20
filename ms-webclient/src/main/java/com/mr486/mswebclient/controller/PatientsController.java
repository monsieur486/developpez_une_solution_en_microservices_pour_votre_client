package com.mr486.mswebclient.controller;

import com.mr486.commun.dto.PatientForm;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.mswebclient.service.PatientApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pages de la liste des patients et de l'ajout d'un patient.
 *
 * <p><b>Exemple :</b> {@code GET /app/patients} liste les patients ; {@code GET /app/patients/ajout}
 * ouvre le formulaire de création.</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class PatientsController {
    private static final String VUE_AJOUT = "patient-ajout";

    private final PatientApiService patientApiService;

    /**
     * Affiche la liste des patients.
     *
     * <p><b>Exemple :</b> avec le jeu de démonstration, quatre lignes de {@code TestNone} à
     * {@code TestEarlyOnset}.</p>
     *
     * @param model modèle de la page
     * @return la vue {@code patients}
     */
    @GetMapping("/patients")
    public String listePatients(Model model) {
        model.addAttribute("patients", patientApiService.listeLesPatients());
        return "patients";
    }

    /**
     * Affiche le formulaire de création, vide.
     *
     * <p><b>Exemple :</b> {@code GET /app/patients/ajout} ouvre un formulaire sans aucun champ rempli.</p>
     *
     * @param model modèle de la page
     * @return la vue {@code patient-ajout}
     */
    @GetMapping("/patients/ajout")
    public String showCreatePatientForm(Model model) {
        model.addAttribute("patient", new PatientForm());
        return VUE_AJOUT;
    }

    /**
     * Enregistre un nouveau patient.
     *
     * <p>Un formulaire invalide ou un patient déjà existant réaffiche le formulaire ; sinon, retour à
     * la liste avec un message de confirmation.</p>
     *
     * <p><b>Exemple :</b> créer deux fois {@code TestNone} réaffiche le formulaire avec le motif du
     * doublon.</p>
     *
     * @param patient           données saisies
     * @param resultatDeLiaison erreurs de validation du formulaire
     * @param model             modèle, pour réafficher le formulaire
     * @param redirection       attributs transmis à la page suivante
     * @return la vue du formulaire, ou la redirection vers la liste
     */
    @PostMapping("/patients/ajout")
    public String ajoutPatientSubmit(
            @Valid @ModelAttribute("patient") PatientForm patient,
            BindingResult resultatDeLiaison,
            Model model,
            RedirectAttributes redirection) {
        if (resultatDeLiaison.hasErrors()) {
            return VUE_AJOUT;
        }
        try {
            patientApiService.creeLePatient(patient);
        } catch (DuplicateException ex) {
            Notifications.avertit(model, ex.getMessage());
            return VUE_AJOUT;
        }
        Notifications.confirme(redirection, "Patient créé.");
        return "redirect:/app/patients";
    }
}
