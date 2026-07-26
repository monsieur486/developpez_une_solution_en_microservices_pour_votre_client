package com.mr486.mswebclient.service;

import com.mr486.mswebclient.dto.Note;
import com.mr486.mswebclient.dto.PageReponse;
import com.mr486.mswebclient.exception.GatewayException;
import java.io.IOException;
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
    void getNotesPagines_appelleLaPasserelleEtEmetLaPage() throws InterruptedException {
        serveur.enqueue(new MockResponse()
                .setBody("{\"content\":[{\"content\":\"fumeur\"}],\"page\":0,\"totalPages\":1,\"totalElements\":1}")
                .addHeader("Content-Type", "application/json"));

        PageReponse<Note> page = service.getNotesPagines(7L, 0).block();

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.aPlusieursPages()).isFalse();
        assertThat(serveur.takeRequest().getPath()).isEqualTo("/ms-notes/patients/7/notes/pagines?page=0");
    }

    @Test
    void createNote_envoieLaNoteEnPost() throws InterruptedException {
        serveur.enqueue(new MockResponse().setResponseCode(200));

        service.createNote(7L, new Note("fumeur")).block();

        assertThat(serveur.takeRequest().getMethod()).isEqualTo("POST");
    }

    @Test
    void getNotesPagines_replieSurUnMessageNominatifQuandLaPasserelleEstInjoignable() throws IOException {
        serveur.shutdown();

        assertThatThrownBy(() -> service.getNotesPagines(7L, 0).block())
                .isInstanceOf(GatewayException.class)
                .hasMessage("ms-notes ne répond pas.");
    }
}
