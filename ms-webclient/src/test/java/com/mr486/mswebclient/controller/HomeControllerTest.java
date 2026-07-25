package com.mr486.mswebclient.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomeControllerTest {

    private final HomeController controller = new HomeController();

    @Test
    void home_retourneLaVueDAccueil() {
        assertThat(controller.home()).isEqualTo("home");
    }

    @Test
    void login_retourneLaVueDeConnexion() {
        assertThat(controller.login()).isEqualTo("login");
    }
}
