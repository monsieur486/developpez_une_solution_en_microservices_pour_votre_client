package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.service.NoteService;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Pages des notes médicales d'un patient et de l'ajout d'une note.
 *
 * <p><b>Exemple :</b> GET /app/patients/7/notes affiche les notes du patient 7 ;
 * POST /app/patients/7/notes en ajoute une nouvelle.</p>
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
@Slf4j
public class NotesController {

    /** Nom du microservice cité dans les messages d'erreur. */
    private static final String MICROSERVICE = "ms-notes";

    private final NoteService noteService;
    private final ErrorResponseTools errorResponseTools;

    /**
     * Affiche les notes médicales d'un patient.
     *
     * <p><b>Exemple :</b> GET /app/patients/7/notes retourne la vue "notes/notes"
     * avec les notes du patient 7 dans le modèle.</p>
     *
     * @param model     le modèle de la vue
     * @param patientId identifiant du patient
     * @return le nom de la vue de la liste des notes
     */
    @GetMapping("/patients/{patientId}/notes")
    public String getNotes(Model model, @PathVariable Long patientId) {
        try {
            model.addAttribute("notes", noteService.getNotesByPatientId(patientId));
            model.addAttribute("patientId", patientId);
        } catch (Exception ex) {
            log.warn("échec de récupération des notes du patient {} : {}", patientId, ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("patientId", patientId);
        }
        return "notes/notes";
    }

    /**
     * Affiche le formulaire d'ajout d'une note.
     *
     * <p><b>Exemple :</b> GET /app/patients/7/notes/ajout retourne la vue
     * "notes/note-ajout" avec une note vierge.</p>
     *
     * @param patientId identifiant du patient concerné
     * @param model     le modèle de la vue
     * @return le nom de la vue du formulaire d'ajout
     */
    @GetMapping("/patients/{patientId}/notes/ajout")
    public String showCreateNoteForm(@PathVariable Long patientId, Model model) {
        model.addAttribute("patientId", patientId);
        model.addAttribute("note", new Note());
        return "notes/note-ajout";
    }

    /**
     * Crée une note pour un patient à partir du formulaire soumis.
     *
     * <p><b>Exemple :</b> POST /app/patients/7/notes crée la note puis redirige
     * vers la liste des notes ; en cas d'échec, réaffiche le formulaire avec le
     * message d'erreur.</p>
     *
     * @param patientId identifiant du patient concerné
     * @param note      la note à créer
     * @param model     le modèle de la vue
     * @return la redirection vers les notes, ou le formulaire en cas d'erreur
     */
    @PostMapping("/patients/{patientId}/notes")
    public String ajoutNotePost(@PathVariable Long patientId, Note note, Model model) {
        try {
            noteService.createNote(patientId, note);
            log.info("note créée pour le patient {}", patientId);
            return "redirect:/app/patients/{patientId}/notes";
        } catch (Exception ex) {
            log.warn("échec de création d'une note pour le patient {} : {}", patientId, ex.getMessage());
            ErrorMessage errorMessage = errorResponseTools.getErrorMessage(ex.getMessage(), MICROSERVICE);
            model.addAttribute("patientId", patientId);
            model.addAttribute("note", note);
            model.addAttribute("errorMessage", errorMessage);
            return "notes/note-ajout";
        }
    }
}
