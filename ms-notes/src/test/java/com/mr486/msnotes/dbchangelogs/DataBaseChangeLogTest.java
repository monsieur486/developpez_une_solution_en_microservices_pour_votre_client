package com.mr486.msnotes.dbchangelogs;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class DataBaseChangeLogTest {

    // MongoDB éphémère démarré par Testcontainers ; @ServiceConnection câble
    // automatiquement spring.data.mongodb.* sur ce conteneur.
    @Container
    @ServiceConnection
    static MongoDBContainer mongoDb = new MongoDBContainer("mongo:8.0.14");

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void videLaCollection() {
        mongoTemplate.getCollection("notes").deleteMany(new Document());
    }

    @Test
    void seed_insereLesNotesDeDemoQuandLaCollectionEstVide() {
        DataBaseChangeLog changeLog = new DataBaseChangeLog(mongoTemplate);

        changeLog.seedDemoNotesIfEmpty();

        assertThat(mongoTemplate.getCollection("notes").countDocuments()).isEqualTo(9);
    }

    @Test
    void seed_neReinserePasQuandLaCollectionContientDejaDesNotes() {
        DataBaseChangeLog changeLog = new DataBaseChangeLog(mongoTemplate);

        changeLog.seedDemoNotesIfEmpty();
        changeLog.seedDemoNotesIfEmpty();

        assertThat(mongoTemplate.getCollection("notes").countDocuments()).isEqualTo(9);
    }

    @Test
    void rollback_videLaCollection() {
        DataBaseChangeLog changeLog = new DataBaseChangeLog(mongoTemplate);
        changeLog.seedDemoNotesIfEmpty();

        changeLog.rollback();

        assertThat(mongoTemplate.getCollection("notes").countDocuments()).isZero();
    }
}
