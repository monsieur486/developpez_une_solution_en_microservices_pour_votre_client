package com.mr486.msnotes.controller;

import com.mr486.msnotes.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.service.NoteService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST des notes d'un patient.
 *
 * <p><b>Exemple :</b> GET /patients/2/notes liste les notes du patient 2 ;
 * POST /patients/2/notes ajoute une note à ce patient.</p>
 */
@RestController
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(title = "Gestion des notes d'un patient API", version = "v1"))
@SecurityRequirement(name = "basicAuth")
public class NoteController {

    private final NoteService noteService;

    /**
     * Récupère toutes les notes d'un patient.
     *
     * <p><b>Exemple :</b> GET /patients/2/notes retourne les notes du patient 2,
     * de la plus récente à la plus ancienne.</p>
     *
     * @param patientId identifiant du patient
     * @return les notes du patient
     */
    @Tag(name = "Récupère toutes les notes d'un patient par son ID")
    @GetMapping(value = "/patients/{patientId}/notes", produces = "application/json")
    public List<Note> getNotes(@PathVariable Long patientId) {
        return noteService.findByPatientId(patientId);
    }

    /**
     * Ajoute une note à un patient.
     *
     * <p><b>Exemple :</b> POST /patients/2/notes avec {@code {"content": "RAS"}}
     * crée la note et retourne l'entité enregistrée.</p>
     *
     * @param patientId identifiant du patient
     * @param noteDto   contenu de la note à créer
     * @return la note créée
     */
    @Tag(name = "Ajoute une note à un patient par son ID")
    @PostMapping(value = "/patients/{patientId}/notes", consumes = "application/json")
    public Note addNote(@PathVariable Long patientId, @Valid @RequestBody NoteDto noteDto) {
        return noteService.save(patientId, noteDto);
    }

}
