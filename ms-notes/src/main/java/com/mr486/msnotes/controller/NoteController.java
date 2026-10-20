package com.mr486.msnotes.controller;

import com.mr486.commun.dto.NoteDto;
import com.mr486.msnotes.mapper.NoteMapper;
import com.mr486.msnotes.service.NoteService;
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

@RestController
@RequestMapping(value = "/patients/{patientId}/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    private final NoteMapper noteMapper;

    @GetMapping
    public ResponseEntity<List<NoteDto>> getNotes(@PathVariable Long patientId) {
        return ResponseEntity.ok(noteMapper.versListeDto(noteService.findByPatientId(patientId)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NoteDto> addNote(
            @PathVariable Long patientId, @Valid @RequestBody NoteDto noteDto) {
        NoteDto creee = noteMapper.versDto(noteService.save(patientId, noteDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }
}
