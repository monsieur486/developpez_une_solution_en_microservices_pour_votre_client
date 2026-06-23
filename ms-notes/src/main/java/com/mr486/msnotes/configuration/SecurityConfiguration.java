package com.mr486.msnotes.configuration;

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
 * Configuration de sécurité : authentification HTTP Basic et utilisateur applicatif
 * unique en mémoire.
 *
 * <p><b>Exemple :</b> l'actuator et la documentation OpenAPI sont publics ; toute
 * autre route exige les identifiants de l'utilisateur applicatif.</p>
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
     * <p><b>Exemple :</b> filter(http) ouvre {@code /actuator/**} et
     * {@code /v3/api-docs/**}, protège le reste et active l'authentification
     * HTTP Basic.</p>
     *
     * @param http le constructeur de configuration HTTP fourni par Spring Security
     * @return la chaîne de filtres construite
     * @throws Exception si la configuration HTTP échoue
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
     * Fournit l'encodeur de mots de passe BCrypt.
     *
     * <p><b>Exemple :</b> passwordEncoder().encode("pasW0rd") retourne une
     * empreinte BCrypt vérifiable.</p>
     *
     * @return l'encodeur de mots de passe BCrypt
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Déclare l'utilisateur applicatif unique, doté du rôle {@code USER}.
     *
     * <p><b>Exemple :</b> users(encoder) crée un utilisateur en mémoire à partir
     * des identifiants configurés.</p>
     *
     * @param enc l'encodeur servant à hacher le mot de passe
     * @return le service de gestion de l'utilisateur applicatif
     */
    @Bean
    UserDetailsService users(PasswordEncoder enc) {
        return new InMemoryUserDetailsManager(
                User.withUsername(appUser).password(enc.encode(appPass)).roles("USER").build()
        );
    }
}
