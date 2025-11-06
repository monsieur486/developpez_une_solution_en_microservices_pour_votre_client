package com.mr486.msnotes.service;

import com.mr486.msnotes.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

  private final NoteRepository noteRepository;

  public List<Note> findByPatientId(Long patientId) {
    return noteRepository.findByPatientIdOrderByCreatedDateDesc(patientId);
  }

  public Note save(Long patientId, NoteDto noteDto) {
    Note note = Note.builder()
            .patientId(patientId)
            .content(noteDto.getContent())
            .createdDate(new java.util.Date())
            .build();
    return noteRepository.save(note);
  }

}
