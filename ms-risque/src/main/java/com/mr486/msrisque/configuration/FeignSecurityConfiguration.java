package com.mr486.msrisque.configuration;

import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Authentifie en HTTP Basic les appels Feign vers {@code ms-patients} et {@code ms-notes}.
 *
 * <p>Les identifiants sont ceux de l'utilisateur technique commun à tous les microservices. Comme
 * la classe porte {@code @Configuration}, Spring la détecte et l'intercepteur s'applique à
 * <b>tous</b> les clients Feign : la mention {@code configuration = …} sur chaque client le rend
 * explicite à la lecture, sans rien changer d'autre.</p>
 *
 * <p><b>Exemple :</b> chaque requête Feign part avec l'en-tête {@code Authorization: Basic …}.</p>
 */
@Configuration
public class FeignSecurityConfiguration {
    /**
     * Intercepteur qui ajoute l'en-tête {@code Authorization} à chaque requête Feign.
     *
     * <p><b>Exemple :</b> avec {@code APP_USERNAME=app_user}, l'en-tête encode {@code app_user:…}.</p>
     *
     * @param utilisateur nom de l'utilisateur technique ({@code security.app-user.username})
     * @param motDePasse son mot de passe ({@code security.app-user.password}), sans valeur par défaut
     * @return l'intercepteur HTTP Basic, en UTF-8
     */
    @Bean
    public RequestInterceptor intercepteurAuthentification(
            @Value("${security.app-user.username}") String utilisateur,
            @Value("${security.app-user.password}") String motDePasse) {
        return new BasicAuthRequestInterceptor(utilisateur, motDePasse, StandardCharsets.UTF_8);
    }
}
