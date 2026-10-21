package com.mr486.mspatients;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@DisplayName("Démarrage du microservice des patients")
class MsPatientsApplicationTests {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("le contexte se charge et le schéma est conforme aux entités")
    void contextLoads() {
    }

    // Les patients de démonstration ne doivent jamais atterrir dans une base réelle par oubli.
    @Test
    @DisplayName("sans contexte Liquibase explicite, aucun patient de démonstration n'est inséré")
    void migration_parDefaut_nInserePasLaFixture() {
        Integer nombreDePatients = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM patients", Integer.class);

        assertThat(nombreDePatients).isZero();
    }
}
