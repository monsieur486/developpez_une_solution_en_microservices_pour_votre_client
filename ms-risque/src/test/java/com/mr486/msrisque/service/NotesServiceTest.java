package com.mr486.msrisque.service;

import com.mr486.msrisque.dto.Note;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class NotesServiceTest {

    private MockWebServer serveur;
    private NotesService notesService;

    @BeforeEach
    void init() throws IOException {
        serveur = new MockWebServer();
        serveur.start();
        WebClient webClient = WebClient.builder().baseUrl(serveur.url("/").toString()).build();
        notesService = new NotesService(webClient);
    }

    @AfterEach
    void arret() throws IOException {
        serveur.shutdown();
    }

    @Test
    void getNotesByPatientId_appelleMsNotesEtRetourneLesNotes() throws InterruptedException {
        serveur.enqueue(new MockResponse()
                .setBody("[{\"content\":\"Le patient est fumeur\"},{\"content\":\"Taille anormale\"}]")
                .addHeader("Content-Type", "application/json"));

        List<Note> notes = notesService.getNotesByPatientId(7L).collectList().block();

        assertThat(notes).hasSize(2);
        assertThat(notes.get(0).getContent()).isEqualTo("Le patient est fumeur");
        assertThat(serveur.takeRequest().getPath()).isEqualTo("/patients/7/notes");
    }

    @Test
    void getNotesByPatientId_retourneUneListeVideSansNote() {
        serveur.enqueue(new MockResponse().setBody("[]")
                .addHeader("Content-Type", "application/json"));

        assertThat(notesService.getNotesByPatientId(7L).collectList().block()).isEmpty();
    }
}
