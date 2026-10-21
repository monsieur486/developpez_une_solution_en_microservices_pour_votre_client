package com.mr486.mswebclient.controller;

import com.mr486.commun.dto.PatientForm;
import com.mr486.commun.exception.DuplicateException;
import com.mr486.mswebclient.mapper.PatientFormMapper;
import com.mr486.mswebclient.service.PatientApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pages d'un patient : consultation et modification de sa fiche.
 *
 * <p><b>Exemple :</b> {@code GET /app/patients/1} affiche la fiche ; {@code GET /app/patients/1/update}
 * ouvre le formulaire pré-rempli.</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class PatientDetailController {
    private static final String VUE_MODIFICATION = "patient-update";

    private static final String ATTRIBUT_ID = "patientId";

    private final PatientApiService patientApiService;
    private final PatientFormMapper patientFormMapper;

    /**
     * Affiche la fiche d'un patient.
     *
     * <p><b>Exemple :</b> un identifiant inconnu affiche la page d'erreur en 404.</p>
     *
     * @param id    identifiant du patient
     * @param model modèle de la page
     * @return la vue {@code patient-detail}
     */
    @GetMapping("/patients/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientApiService.recupereLePatient(id));
        model.addAttribute(ATTRIBUT_ID, id);
        return "patient-detail";
    }

    /**
     * Affiche le formulaire de modification, pré-rempli avec la fiche actuelle.
     *
     * <p><b>Exemple :</b> le formulaire de {@code TestNone} s'ouvre avec sa date de naissance 1966-12-31.</p>
     *
     * @param id    identifiant du patient
     * @param model modèle de la page
     * @return la vue {@code patient-update}
     */
    @GetMapping("/patients/{id}/update")
    public String updatePatientForm(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientFormMapper.versFormulaire(patientApiService.recupereLePatient(id)));
        model.addAttribute(ATTRIBUT_ID, id);
        return VUE_MODIFICATION;
    }

    /**
     * Enregistre la modification d'une fiche.
     *
     * <p>Un formulaire invalide ou une identité déjà prise réaffiche le formulaire ; sinon, retour à
     * la fiche avec un message de confirmation.</p>
     *
     * <p><b>Exemple :</b> changer l'adresse redirige vers {@code /app/patients/1} avec « Fiche patient
     * mise à jour. ».</p>
     *
     * @param id               identifiant du patient
     * @param patient          données saisies
     * @param resultatDeLiaison erreurs de validation du formulaire
     * @param model            modèle, pour réafficher le formulaire
     * @param redirection      attributs transmis à la page suivante
     * @return la vue du formulaire, ou la redirection vers la fiche
     */
    @PostMapping("/patients/{id}/update")
    public String updatePatient(
            @PathVariable Long id,
            @Valid @ModelAttribute("patient") PatientForm patient,
            BindingResult resultatDeLiaison,
            Model model,
            RedirectAttributes redirection) {
        if (resultatDeLiaison.hasErrors()) {
            model.addAttribute(ATTRIBUT_ID, id);
            return VUE_MODIFICATION;
        }
        try {
            patientApiService.metAJourLePatient(id, patient);
        } catch (DuplicateException ex) {
            model.addAttribute(ATTRIBUT_ID, id);
            Notifications.avertit(model, ex.getMessage());
            return VUE_MODIFICATION;
        }
        Notifications.confirme(redirection, "Fiche patient mise à jour.");
        return "redirect:/app/patients/" + id;
    }
}
