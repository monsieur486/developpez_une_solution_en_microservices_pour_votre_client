package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.service.NoteService;
import com.mr486.mswebclient.tools.ErrorResponseTools;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotesControllerTest {

    private final NoteService noteService = mock(NoteService.class);
    private final ErrorResponseTools errorResponseTools = mock(ErrorResponseTools.class);
    private final NotesController controller = new NotesController(noteService, errorResponseTools);

    private final Model model = new ExtendedModelMap();

    @Test
    void getNotes_remplitLeModeleEtRetourneLaListe() {
        List<Note> attendues = List.of(new Note("fumeur"));
        when(noteService.getNotesByPatientId(7L)).thenReturn(attendues);

        String vue = controller.getNotes(model, 7L);

        assertThat(vue).isEqualTo("notes/notes");
        assertThat(model.getAttribute("notes")).isEqualTo(attendues);
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
    }

    @Test
    void getNotes_afficheLErreurQuandLeServiceEchoue() {
        when(noteService.getNotesByPatientId(7L)).thenThrow(new RuntimeException("boom"));
        ErrorMessage erreur = new ErrorMessage(503, "ms-notes ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.getNotes(model, 7L);

        assertThat(vue).isEqualTo("notes/notes");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
    }

    @Test
    void showCreateNoteForm_prepareUneNoteVierge() {
        String vue = controller.showCreateNoteForm(7L, model);

        assertThat(vue).isEqualTo("notes/note-ajout");
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
        assertThat(model.getAttribute("note")).isInstanceOf(Note.class);
    }

    @Test
    void ajoutNotePost_redirigeVersLesNotesApresCreation() {
        String vue = controller.ajoutNotePost(7L, new Note("fumeur"), model);

        assertThat(vue).isEqualTo("redirect:/app/patients/{patientId}/notes");
    }

    @Test
    void ajoutNotePost_reafficheLeFormulaireQuandLaCreationEchoue() {
        Note note = new Note("fumeur");
        doThrow(new RuntimeException("boom")).when(noteService).createNote(eq(7L), any(Note.class));
        ErrorMessage erreur = new ErrorMessage(503, "ms-notes ne répond pas.");
        when(errorResponseTools.getErrorMessage(anyString(), anyString())).thenReturn(erreur);

        String vue = controller.ajoutNotePost(7L, note, model);

        assertThat(vue).isEqualTo("notes/note-ajout");
        assertThat(model.getAttribute("note")).isEqualTo(note);
        assertThat(model.getAttribute("errorMessage")).isEqualTo(erreur);
    }
}
