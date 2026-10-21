package com.mr486.msnotes;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.model.Filters;
import java.time.Instant;
import java.util.Date;
import org.bson.Document;
import org.bson.conversions.Bson;
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

// La variable passe par le placeholder de application.yml, comme en production via le .env.
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.data.mongodb.username=",
        "spring.data.mongodb.password=",
        "spring.data.mongodb.authentication-database=",
        "MONGOCK_PROFILES=demo"})
@DisplayName("Notes de démonstration avec MONGOCK_PROFILES=demo")
class DonneesDeDemonstrationTest {
    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0.14");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("le profil « demo » insère les neuf notes de démonstration")
    void profilDemo_insereLesNotesDeDemonstration() {
        assertThat(mongoTemplate.getCollection("notes").countDocuments()).isEqualTo(9);
    }

    // Non-régression : datées dans le futur, elles passaient devant une note saisie juste après le démarrage.
    @Test
    @DisplayName("les notes de démonstration sont toutes datées dans le passé")
    void profilDemo_dateLesNotesDansLePasse() {
        Bson dansLeFutur = Filters.gt("createdDate", Date.from(Instant.now()));

        assertThat(mongoTemplate.getCollection("notes").countDocuments(dansLeFutur)).isZero();
    }

    // Contre-épreuve du test par défaut : prouve que le journal est bien lu au bon endroit.
    @Test
    @DisplayName("la migration de démonstration est inscrite au journal de Mongock")
    void profilDemo_journaliseLaMigration() {
        assertThat(mongoTemplate.getCollection("mongockChangeLog")
                .countDocuments(new Document("changeId", "seed-demo-notes"))).isPositive();
    }
}
