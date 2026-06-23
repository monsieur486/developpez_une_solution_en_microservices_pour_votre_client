package com.mr486.msrisque.service;

import com.mr486.msrisque.client.NoteClient;
import com.mr486.msrisque.dto.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    @Mock
    private NoteClient noteClient;

    @InjectMocks
    private NotesService notesService;

    @Test
    void getNotesByPatientId_delegueAuClient() {
        List<Note> notes = List.of(Note.builder().content("taille").build());
        when(noteClient.getNotesByPatientId(7L)).thenReturn(notes);

        assertThat(notesService.getNotesByPatientId(7L)).isSameAs(notes);
    }
}
