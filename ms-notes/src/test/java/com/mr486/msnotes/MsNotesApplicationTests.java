package com.mr486.msnotes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class MsNotesApplicationTests {

    // MongoDB éphémère démarré par Testcontainers ; @ServiceConnection câble
    // automatiquement spring.data.mongodb.* sur ce conteneur (plus besoin d'un Mongo local).
    @Container
    @ServiceConnection
    static MongoDBContainer mongoDb = new MongoDBContainer("mongo:8.0.14");

    @Test
    void contextLoads() {
    }
}
