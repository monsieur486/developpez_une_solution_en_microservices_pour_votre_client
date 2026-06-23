package com.mr486.msnotes.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI/Swagger : déclare l'authentification HTTP Basic et le
 * préfixe de serveur exposé dans la documentation.
 *
 * <p><b>Exemple :</b> la documentation générée est servie sous {@code /ms-notes}
 * et requiert le schéma de sécurité {@code basicAuth}.</p>
 */
@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        name = "basicAuth",
        scheme = "basic")
public class SpringdocConfig {

    /**
     * Construit la définition OpenAPI exposée par le service.
     *
     * <p><b>Exemple :</b> api() retourne une OpenAPI dont le serveur de base est
     * {@code /ms-notes}.</p>
     *
     * @return la définition OpenAPI du service
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/ms-notes")));
    }
}
