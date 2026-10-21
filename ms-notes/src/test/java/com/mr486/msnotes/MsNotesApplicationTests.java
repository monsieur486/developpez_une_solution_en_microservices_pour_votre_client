package com.mr486.msnotes;

import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.data.mongodb.username=",
        "spring.data.mongodb.password=",
        "spring.data.mongodb.authentication-database="})
@DisplayName("Démarrage du microservice des notes")
class MsNotesApplicationTests {
    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0.14");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("le contexte se charge et Mongock démarre sans erreur")
    void contextLoads() {
    }

    // Les notes de démonstration ne doivent jamais atterrir dans une base réelle par oubli.
    @Test
    @DisplayName("sans profil « demo », aucune note de démonstration n'est insérée")
    void migration_parDefaut_nInserePasLesNotesDeDemonstration() {
        assertThat(mongoTemplate.getCollection("notes").countDocuments()).isZero();
        // Écartée, la migration n'est pas inscrite au journal : activer « demo » plus tard l'appliquera.
        assertThat(mongoTemplate.getCollection("mongockChangeLog")
                .countDocuments(new Document("changeId", "seed-demo-notes"))).isZero();
    }
}
