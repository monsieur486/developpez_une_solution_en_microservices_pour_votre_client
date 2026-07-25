package com.mr486.msrisque.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration de sécurité réactive du microservice : authentification HTTP
 * Basic et ouverture des endpoints techniques (actuator, documentation OpenAPI).
 *
 * <p><b>Exemple :</b> /actuator/** et /v3/api-docs/** sont accessibles sans
 * authentification ; toute autre route exige un utilisateur authentifié.</p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${security.app-user.username}")
    private String appUser;

    @Value("${security.app-user.password}")
    private String appPass;

    /**
     * Définit la chaîne de filtres de sécurité réactive.
     *
     * <p><b>Exemple :</b> une requête sur /patients/7/evaluation sans
     * authentification est rejetée en 401.</p>
     *
     * @param http le constructeur de configuration HTTP réactive
     * @return la chaîne de filtres de sécurité
     */
    @Bean
    SecurityWebFilterChain filter(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(reg -> reg
                        .pathMatchers(
                                "/actuator/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Fournit l'encodeur de mots de passe (BCrypt).
     *
     * <p><b>Exemple :</b> passwordEncoder().encode("pasW0rd") retourne un haché
     * BCrypt.</p>
     *
     * @return l'encodeur de mots de passe
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Déclare l'utilisateur applicatif en mémoire (variante réactive).
     *
     * <p><b>Exemple :</b> users(enc) crée un utilisateur de rôle ADMIN à partir
     * des identifiants configurés.</p>
     *
     * @param enc l'encodeur de mots de passe utilisé pour hacher le mot de passe
     * @return le gestionnaire réactif d'utilisateurs en mémoire
     */
    @Bean
    MapReactiveUserDetailsService users(PasswordEncoder enc) {
        return new MapReactiveUserDetailsService(
                User.withUsername(appUser).password(enc.encode(appPass)).roles("ADMIN").build()
        );
    }
}
