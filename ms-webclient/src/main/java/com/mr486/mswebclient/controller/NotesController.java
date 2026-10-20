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

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class NotesController {
    private static final String VUE_AJOUT = "note-ajout";

    private static final String ATTRIBUT_ID = "patientId";

    private final NoteApiService noteApiService;

    @GetMapping("/patients/{patientId}/notes/ajout")
    public String showCreateNoteForm(@PathVariable Long patientId, Model model) {
        model.addAttribute("note", new NoteForm());
        model.addAttribute(ATTRIBUT_ID, patientId);
        return VUE_AJOUT;
    }

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
