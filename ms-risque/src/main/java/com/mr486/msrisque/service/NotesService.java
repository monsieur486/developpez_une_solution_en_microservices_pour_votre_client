package com.mr486.msrisque.service;

import com.mr486.msrisque.client.NoteClient;
import com.mr486.msrisque.dto.Note;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotesService {

  private final NoteClient noteClient;

  public List<Note> getNotesByPatientId(Long patientId) {
    return noteClient.getNotesByPatientId(patientId);
  }
}
