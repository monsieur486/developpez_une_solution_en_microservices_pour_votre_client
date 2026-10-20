package com.mr486.commun.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        name = "basicAuth",
        scheme = "basic")
public class DocumentationApiConfiguration {

    @Value("${spring.application.name:application}")
    private String nomDuMicroservice;

    @Bean
    public OpenAPI contratOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/" + nomDuMicroservice)));
    }
}
