package com.mr486.mswebclient.controller;

import com.mr486.commun.dto.NoteForm;
import com.mr486.mswebclient.service.NoteApiService;
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
 * Pages d'ajout d'une note au dossier d'un patient.
 *
 * <p>L'historique, lui, s'affiche sur la fiche du patient ({@link PatientDetailController}).</p>
 *
 * <p><b>Exemple :</b> {@code GET /app/patients/3/notes/ajout} ouvre le formulaire ; l'envoi ramène
 * sur la fiche du patient 3 avec « Note ajoutée au dossier. ».</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class NotesController {
    private static final String VUE_AJOUT = "note-ajout";

    private static final String ATTRIBUT_ID = "patientId";

    private final NoteApiService noteApiService;

    /**
     * Affiche le formulaire d'ajout, vide.
     *
     * <p><b>Exemple :</b> le formulaire du patient 3 s'ouvre avec un champ de texte vide.</p>
     *
     * @param patientId identifiant du patient
     * @param model     modèle de la page
     * @return la vue {@code note-ajout}
     */
    @GetMapping("/patients/{patientId}/notes/ajout")
    public String showCreateNoteForm(@PathVariable Long patientId, Model model) {
        model.addAttribute("note", new NoteForm());
        model.addAttribute(ATTRIBUT_ID, patientId);
        return VUE_AJOUT;
    }

    /**
     * Enregistre la note saisie.
     *
     * <p>Une note vide ou trop longue réaffiche le formulaire avec son erreur ; sinon, retour à la
     * fiche avec un message de confirmation.</p>
     *
     * <p><b>Exemple :</b> « Poids en hausse » redirige vers {@code /app/patients/3}.</p>
     *
     * @param patientId         identifiant du patient
     * @param note              note saisie
     * @param resultatDeLiaison erreurs de validation du formulaire
     * @param model             modèle, pour réafficher le formulaire
     * @param redirection       attributs transmis à la page suivante
     * @return la vue du formulaire, ou la redirection vers la fiche
     */
    @PostMapping("/patients/{patientId}/notes")
    public String ajoutNoteSubmit(
            @PathVariable Long patientId,
            @Valid @ModelAttribute("note") NoteForm note,
            BindingResult resultatDeLiaison,
            Model model,
            RedirectAttributes redirection) {
        if (resultatDeLiaison.hasErrors()) {
            model.addAttribute(ATTRIBUT_ID, patientId);
            return VUE_AJOUT;
        }
        noteApiService.ajouteUneNote(patientId, note);
        Notifications.confirme(redirection, "Note ajoutée au dossier.");
        return "redirect:/app/patients/" + patientId;
    }
}
