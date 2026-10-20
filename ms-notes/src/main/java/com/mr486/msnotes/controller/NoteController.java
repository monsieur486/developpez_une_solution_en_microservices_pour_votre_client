package com.mr486.msnotes.controller;

import com.mr486.commun.dto.ErrorResponse;
import com.mr486.commun.dto.NoteDto;
import com.mr486.commun.dto.NoteForm;
import com.mr486.msnotes.mapper.NoteMapper;
import com.mr486.msnotes.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST des notes d'un patient, exposée derrière la gateway sous
 * {@code /ms-notes/patients/{patientId}/notes}.
 *
 * <p>Toutes les routes exigent l'authentification HTTP Basic. Une note ne se modifie ni ne se
 * supprime : le besoin ne prévoit que la consultation et l'ajout. Les erreurs (400) sont mises en
 * forme par le gestionnaire d'erreurs commun de {@code ms-commun}.</p>
 *
 * <p><b>Exemple :</b> {@code GET /patients/3/notes} avec des identifiants valides renvoie 200 et
 * l'historique ; sans identifiants, 401.</p>
 */
@RestController
@RequestMapping(value = "/patients/{patientId}/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Historique des notes des praticiens sur un patient")
@SecurityRequirement(name = "basicAuth")
public class NoteController {
    private final NoteService noteService;
    private final NoteMapper noteMapper;

    /**
     * Renvoie l'historique des notes d'un patient.
     *
     * <p><b>Exemple :</b> {@code GET /patients/3/notes} renvoie 200 et les notes, de la plus récente
     * à la plus ancienne ; un patient sans note donne un tableau vide.</p>
     *
     * @param patientId identifiant du patient
     * @return 200 et l'historique du patient
     */
    @Operation(summary = "Renvoie l'historique des notes d'un patient",
            description = "De la plus récente à la plus ancienne ; liste vide si le patient n'a aucune note.")
    @ApiResponse(responseCode = "200", description = "Notes du patient, éventuellement vides")
    @ApiResponse(responseCode = "401", description = "Identifiants absents ou invalides", content = @Content)
    @GetMapping
    public ResponseEntity<List<NoteDto>> getNotes(@PathVariable Long patientId) {
        return ResponseEntity.ok(noteMapper.versListeDto(noteService.findByPatientId(patientId)));
    }

    /**
     * Ajoute une note au dossier d'un patient.
     *
     * <p><b>Exemple :</b> {@code POST /patients/3/notes} avec {@code {"content":"Poids en hausse"}}
     * renvoie 201 et la note datée ; un contenu vide renvoie 400.</p>
     *
     * @param patientId identifiant du patient
     * @param noteForm  note saisie
     * @return 201 et la note enregistrée
     */
    @Operation(summary = "Ajoute une note au dossier d'un patient",
            description = "La date d'enregistrement est attribuée par le service.")
    @ApiResponse(responseCode = "201", description = "Note enregistrée et datée")
    @ApiResponse(responseCode = "400", description = "Contenu vide ou de plus de 5000 caractères",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Identifiants absents ou invalides", content = @Content)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NoteDto> addNote(
            @PathVariable Long patientId, @Valid @RequestBody NoteForm noteForm) {
        NoteDto creee = noteMapper.versDto(noteService.save(patientId, noteForm));
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }
}
