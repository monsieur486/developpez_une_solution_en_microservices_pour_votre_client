package com.mr486.msnotes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mr486.msnotes.model.Note;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataMongoTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.data.mongodb.username=",
        "spring.data.mongodb.password=",
        "spring.data.mongodb.authentication-database="})
@DisplayName("Historique des notes en base")
class NoteRepositoryTest {
    private static final Long PATIENT_RISQUE_LIMITE = 2L;

    private static final Long PATIENT_EN_DANGER = 3L;

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0.14");

    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void videLaCollection() {
        noteRepository.deleteAll();
    }

    // La requête est dérivée de son nom : seul un vrai MongoDB prouve le filtre et le tri.
    @Test
    @DisplayName("l'historique ne contient que les notes du patient, de la plus récente à la plus ancienne")
    void findByPatientId_filtreParPatientEtTrieDuPlusRecent() {
        noteRepository.saveAll(List.of(
                uneNote(PATIENT_RISQUE_LIMITE, "première visite", "2026-01-10T09:00:00Z"),
                uneNote(PATIENT_EN_DANGER, "autre patient", "2026-02-10T09:00:00Z"),
                uneNote(PATIENT_RISQUE_LIMITE, "dernière visite", "2026-03-10T09:00:00Z")));

        List<Note> historique = noteRepository.findByPatientIdOrderByCreatedDateDesc(PATIENT_RISQUE_LIMITE);

        assertThat(historique).extracting(Note::getContent)
                .containsExactly("dernière visite", "première visite");
    }

    // Note d'un patient datée de l'instant donné.
    private Note uneNote(Long patientId, String contenu, String instant) {
        return Note.builder().patientId(patientId).content(contenu)
                .createdDate(Instant.parse(instant)).build();
    }
}
