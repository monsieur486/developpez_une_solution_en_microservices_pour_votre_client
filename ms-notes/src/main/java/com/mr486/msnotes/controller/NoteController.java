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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@OpenAPIDefinition(info = @Info(title = "Gestion des notes d'un patient API", version = "v1"))
@SecurityRequirement(name = "basicAuth")
public class NoteController {

  private final NoteService noteService;

  @Tag(name = "Récupère toutes les notes d'un patient par son ID")
  @GetMapping(value = "/patients/{patientId}/notes", produces = "application/json")
  public List<Note> getNotes(@PathVariable Long patientId) {
    return noteService.findByPatientId(patientId);
  }

  @Tag(name = "Ajoute une note à un patient par son ID")
  @PostMapping(value = "/patients/{patientId}/notes", consumes = "application/json")
  public Note addNote(@PathVariable Long patientId, @Valid @RequestBody NoteDto noteDto) {
    return noteService.save(patientId, noteDto);
  }

}
