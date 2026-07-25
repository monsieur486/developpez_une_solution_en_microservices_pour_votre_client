package com.mr486.mswebclient.controller;

import com.mr486.mswebclient.dto.ErrorMessage;
import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.exception.GatewayException;
import com.mr486.mswebclient.service.NoteService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotesControllerTest {

    private static final GatewayException PANNE =
            new GatewayException(new ErrorMessage(503, "ms-notes ne répond pas."));

    private final NoteService noteService = mock(NoteService.class);
    private final NotesController controller = new NotesController(noteService);

    private final Model model = new ExtendedModelMap();

    @Test
    void getNotes_remplitLeModeleEtRetourneLaListe() {
        List<Note> attendues = List.of(new Note("fumeur"));
        when(noteService.getNotesByPatientId(7L)).thenReturn(Flux.fromIterable(attendues));

        String vue = controller.getNotes(model, 7L).block();

        assertThat(vue).isEqualTo("notes/notes");
        assertThat(model.getAttribute("notes")).isEqualTo(attendues);
        assertThat(model.getAttribute("patientId")).isEqualTo(7L);
    }

    @Test
    void getNotes_afficheLErreurQuandLeServiceEchoue() {
        when(noteService.getNotesByPatientId(7L)).thenReturn(Flux.error(PANNE));

        String vue = controller.getNotes(model, 7L).block();

        assertThat(vue).isEqualTo("notes/notes");
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
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
        when(noteService.createNote(eq(7L), any(Note.class))).thenReturn(Mono.empty());

        String vue = controller.ajoutNotePost(7L, new Note("fumeur"), model).block();

        assertThat(vue).isEqualTo("redirect:/app/patients/7/notes");
    }

    @Test
    void ajoutNotePost_reafficheLeFormulaireQuandLaCreationEchoue() {
        Note note = new Note("fumeur");
        when(noteService.createNote(eq(7L), any(Note.class))).thenReturn(Mono.error(PANNE));

        String vue = controller.ajoutNotePost(7L, note, model).block();

        assertThat(vue).isEqualTo("notes/note-ajout");
        assertThat(model.getAttribute("note")).isEqualTo(note);
        assertThat(model.getAttribute("errorMessage")).isEqualTo(PANNE.getErrorMessage());
    }
}
