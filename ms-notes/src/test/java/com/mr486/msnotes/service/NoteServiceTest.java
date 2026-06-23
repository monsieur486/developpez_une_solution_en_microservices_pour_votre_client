package com.mr486.msnotes.service;

import com.mr486.msnotes.dto.NoteDto;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void findByPatientId_retourneLesNotesDuPatient() {
        when(noteRepository.findByPatientIdOrderByCreatedDateDesc(2L))
                .thenReturn(List.of(new Note(), new Note()));

        assertThat(noteService.findByPatientId(2L)).hasSize(2);
    }

    @Test
    void save_construitUneNoteDateePuisLaPersiste() {
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note enregistree = noteService.save(2L, NoteDto.builder().content("RAS").build());

        assertThat(enregistree.getPatientId()).isEqualTo(2L);
        assertThat(enregistree.getContent()).isEqualTo("RAS");
        assertThat(enregistree.getCreatedDate()).isNotNull();
    }
}
