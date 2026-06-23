package com.mr486.mspatients.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration de la documentation OpenAPI (Swagger) du microservice.
 *
 * <p><b>Exemple :</b> déclare le schéma de sécurité "basicAuth" et fixe le préfixe
 * de serveur "/ms-patients".</p>
 */
@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        name = "basicAuth",
        scheme = "basic")
public class SpringdocConfig {

    /**
     * Construit la définition OpenAPI exposée par le microservice.
     *
     * <p><b>Exemple :</b> api() retourne une OpenAPI dont le serveur est
     * "/ms-patients".</p>
     *
     * @return la définition OpenAPI
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/ms-patients")));
    }
}
