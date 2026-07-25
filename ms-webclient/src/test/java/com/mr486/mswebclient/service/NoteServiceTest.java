package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Note;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NoteServiceTest {

    private static final String BASE = "http://gateway";

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final NoteService service = new NoteService(restTemplate, BASE);

    @Test
    void getNotesByPatientId_appelleLaPasserelleEtRetourneLesNotes() {
        List<Note> attendues = List.of(new Note("fumeur"));
        when(restTemplate.exchange(eq(BASE + "/ms-notes/patients/7/notes"), eq(HttpMethod.GET),
                isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(attendues));

        List<Note> notes = service.getNotesByPatientId(7L);

        assertThat(notes).isEqualTo(attendues);
    }

    @Test
    void createNote_envoieLaNoteEnPost() {
        Note note = new Note("fumeur");
        when(restTemplate.exchange(eq(BASE + "/ms-notes/patients/7/notes"), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok().build());

        service.createNote(7L, note);

        verify(restTemplate).exchange(eq(BASE + "/ms-notes/patients/7/notes"), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
}
