package com.mr486.commun.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Socle commun de la documentation OpenAPI des microservices, activé par {@code @Import}.
 *
 * <p>Il déclare le schéma de sécurité {@code basicAuth}, que chaque contrôleur référence par
 * {@code @SecurityRequirement}, et le serveur du contrat : le nom du microservice, lu dans
 * {@code spring.application.name}. Les essais lancés depuis l'interface Swagger visent ainsi la
 * gateway ({@code /ms-patients/patients}), qui retire ce préfixe avant de transmettre, au lieu
 * d'appeler le service en direct.</p>
 *
 * <p><b>Exemple :</b> dans {@code ms-notes}, le contrat annonce le serveur {@code /ms-notes} et
 * un bouton « Authorize » en HTTP Basic.</p>
 */
@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        name = "basicAuth",
        scheme = "basic")
public class DocumentationApiConfiguration {

    /** Nom d'inscription du microservice, qui sert de préfixe de route dans la gateway. */
    @Value("${spring.application.name:application}")
    private String nomDuMicroservice;

    /**
     * Contrat OpenAPI de base, complété par springdoc à partir des contrôleurs.
     *
     * <p><b>Exemple :</b> pour {@code ms-risque}, {@code servers[0].url} vaut {@code /ms-risque}.</p>
     *
     * @return le contrat, avec pour seul serveur le préfixe du microservice dans la gateway
     */
    @Bean
    public OpenAPI contratOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/" + nomDuMicroservice)));
    }
}
