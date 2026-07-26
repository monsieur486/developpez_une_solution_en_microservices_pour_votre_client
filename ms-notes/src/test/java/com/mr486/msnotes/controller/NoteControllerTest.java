package com.mr486.msnotes.controller;

import com.mr486.msnotes.dto.NoteDto;
import com.mr486.msnotes.dto.PageReponse;
import com.mr486.msnotes.model.Note;
import com.mr486.msnotes.service.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    @Test
    void getNotes_retourneLesNotesDuService() {
        when(noteService.findByPatientId(2L)).thenReturn(List.of(new Note()));

        List<Note> notes = noteController.getNotes(2L);

        assertThat(notes).hasSize(1);
    }

    @Test
    void getNotesPagines_retourneLaPageDuService() {
        when(noteService.findByPatientId(2L, 0)).thenReturn(new PageImpl<>(List.of(new Note())));

        PageReponse<Note> page = noteController.getNotesPagines(2L, 0);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void addNote_delegueLaCreationAuService() {
        Note cree = new Note();
        when(noteService.save(eq(2L), any(NoteDto.class))).thenReturn(cree);

        Note retour = noteController.addNote(2L, NoteDto.builder().content("RAS").build());

        assertThat(retour).isSameAs(cree);
    }
}
