package com.mr486.mswebclient.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité du client web : formulaire de connexion, pages
 * publiques (accueil) et déconnexion.
 *
 * <p><b>Exemple :</b> / et /home sont accessibles sans authentification ; toute
 * autre page redirige vers le formulaire /login.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${app.auth.username}")
    private String appUser;

    @Value("${app.auth.password}")
    private String appPass;

    /**
     * Définit la chaîne de filtres de sécurité.
     *
     * <p><b>Exemple :</b> une requête sur /app/patients sans session redirige
     * vers /login ; une connexion réussie mène à /app/patients.</p>
     *
     * @param http le constructeur de configuration HTTP
     * @return la chaîne de filtres de sécurité
     * @throws Exception si la configuration échoue
     */
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/home"
                        )
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/app/patients", true)
                                .permitAll()
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .deleteCookies("JSESSIONID")
                                .permitAll()
                );
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
     * <p><b>Exemple :</b> users(enc) crée un utilisateur de rôle USER à partir
     * des identifiants configurés.</p>
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
