package com.mr486.msnotes.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringdocConfigTest {

    private final SpringdocConfig configuration = new SpringdocConfig();

    @Test
    void api_exposeLeServeurMsNotes() {
        OpenAPI api = configuration.api();

        assertThat(api.getServers()).hasSize(1);
        assertThat(api.getServers().get(0).getUrl()).isEqualTo("/ms-notes");
    }
}
