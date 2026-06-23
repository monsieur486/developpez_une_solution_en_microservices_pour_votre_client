package com.mr486.mspatients.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité du microservice : authentification HTTP Basic et
 * ouverture des endpoints techniques (actuator, documentation OpenAPI).
 *
 * <p><b>Exemple :</b> /actuator/** et /v3/api-docs/** sont accessibles sans
 * authentification ; toute autre route exige un utilisateur authentifié.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${security.app-user.username}")
    private String appUser;

    @Value("${security.app-user.password}")
    private String appPass;

    /**
     * Définit la chaîne de filtres de sécurité.
     *
     * <p><b>Exemple :</b> une requête sur /patients sans authentification est
     * rejetée en 401.</p>
     *
     * @param http le constructeur de configuration HTTP
     * @return la chaîne de filtres de sécurité
     * @throws Exception si la configuration échoue
     */
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(
                                "/actuator/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
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
     * Déclare l'utilisateur applicatif en mémoire.
     *
     * <p><b>Exemple :</b> users(enc) crée un utilisateur de rôle USER à partir des
     * identifiants configurés.</p>
     *
     * @param enc l'encodeur de mots de passe utilisé pour hacher le mot de passe
     * @return le gestionnaire d'utilisateurs en mémoire
     */
    @Bean
    UserDetailsService users(PasswordEncoder enc) {
        return new InMemoryUserDetailsManager(
                User.withUsername(appUser).password(enc.encode(appPass)).roles("USER").build()
        );
    }
}
