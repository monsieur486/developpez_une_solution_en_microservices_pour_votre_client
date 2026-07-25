package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.exception.GatewayException;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoteServiceTest {

    private MockWebServer serveur;
    private NoteService service;

    @BeforeEach
    void init() throws IOException {
        serveur = new MockWebServer();
        serveur.start();
        service = new NoteService(WebClient.builder().baseUrl(serveur.url("/").toString()).build());
    }

    @AfterEach
    void arret() throws IOException {
        serveur.shutdown();
    }

    @Test
    void getNotesByPatientId_appelleLaPasserelleEtEmetLesNotes() throws InterruptedException {
        serveur.enqueue(new MockResponse().setBody("[{\"content\":\"fumeur\"}]")
                .addHeader("Content-Type", "application/json"));

        List<Note> notes = service.getNotesByPatientId(7L).collectList().block();

        assertThat(notes).hasSize(1);
        assertThat(serveur.takeRequest().getPath()).isEqualTo("/ms-notes/patients/7/notes");
    }

    @Test
    void createNote_envoieLaNoteEnPost() throws InterruptedException {
        serveur.enqueue(new MockResponse().setResponseCode(200));

        service.createNote(7L, new Note("fumeur")).block();

        assertThat(serveur.takeRequest().getMethod()).isEqualTo("POST");
    }

    @Test
    void getNotesByPatientId_replieSurUnMessageNominatifQuandLaPasserelleEstInjoignable() throws IOException {
        serveur.shutdown();

        assertThatThrownBy(() -> service.getNotesByPatientId(7L).collectList().block())
                .isInstanceOf(GatewayException.class)
                .hasMessage("ms-notes ne répond pas.");
    }
}
