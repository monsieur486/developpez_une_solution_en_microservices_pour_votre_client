package com.mr486.msnotes.service;

import com.mr486.commun.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.repository.NoteRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {
    private final NoteRepository noteRepository;

    private final Clock horloge;

    public List<Note> findByPatientId(Long patientId) {
        List<Note> notes = noteRepository.findByPatientIdOrderByCreatedDateDesc(patientId);
        if (notes.isEmpty()) {
            log.debug("aucune note enregistrée pour le patient {}", patientId);
        }
        return notes;
    }

    public Note save(Long patientId, NoteDto noteDto) {
        Note enregistree = noteRepository.save(construitNote(patientId, noteDto));
        log.info("note créée pour le patient {}", patientId);
        return enregistree;
    }

    private Note construitNote(Long patientId, NoteDto noteDto) {
        log.debug("construction d'une note pour le patient {}", patientId);
        return Note.builder()
                .patientId(patientId)
                .content(noteDto.getContent())
                .createdDate(Instant.now(horloge))
                .build();
    }
}
