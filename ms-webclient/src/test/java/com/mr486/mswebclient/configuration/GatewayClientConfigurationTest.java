package com.mr486.mswebclient.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayClientConfigurationTest {

    private final GatewayClientConfiguration configuration = new GatewayClientConfiguration();

    @Test
    void restTemplate_porteLAuthentificationBasic() {
        RestTemplate restTemplate = configuration.restTemplate("user", "pass");

        assertThat(restTemplate.getInterceptors())
                .hasExactlyElementsOfTypes(BasicAuthenticationInterceptor.class);
    }

    @Test
    void objectMapper_indenteLaSortieJson() {
        ObjectMapper mapper = configuration.objectMapper();

        assertThat(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
    }
}
