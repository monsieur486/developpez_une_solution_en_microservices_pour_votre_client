package com.mr486.mswebclient.configuration;

import java.net.URI;
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
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

/**
 * Configuration de sécurité réactive du client web : formulaire de connexion,
 * pages publiques (accueil) et déconnexion.
 *
 * <p><b>Exemple :</b> / et /home sont accessibles sans authentification ; toute
 * autre page redirige vers le formulaire /login.</p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${app.auth.username}")
    private String appUser;

    @Value("${app.auth.password}")
    private String appPass;

    /**
     * Définit la chaîne de filtres de sécurité réactive.
     *
     * <p><b>Exemple :</b> une requête sur /app/patients sans session redirige
     * vers /login ; une connexion réussie mène à /app/patients.</p>
     *
     * @param http le constructeur de configuration HTTP réactive
     * @return la chaîne de filtres de sécurité
     */
    @Bean
    SecurityWebFilterChain filter(ServerHttpSecurity http) {
        http
                // contrairement au servlet, WebFlux n'active pas l'authentification anonyme
                // par défaut ; sans elle, le menu (sec:authorize="isAnonymous()") ne peut
                // pas afficher le lien de connexion aux visiteurs non identifiés
                .anonymous(Customizer.withDefaults())
                .authorizeExchange(auth -> auth
                        .pathMatchers(
                                "/",
                                "/home",
                                // contrairement au servlet, loginPage() ne rend pas /login public
                                "/login"
                        )
                        .permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(
                                new RedirectServerAuthenticationSuccessHandler("/app/patients"))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(deconnexionVersAccueil())
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
     * Déclare l'utilisateur applicatif en mémoire (variante réactive).
     *
     * <p><b>Exemple :</b> users(enc) crée un utilisateur de rôle USER à partir
     * des identifiants configurés.</p>
     *
     * @param enc l'encodeur de mots de passe utilisé pour hacher le mot de passe
     * @return le gestionnaire réactif d'utilisateurs en mémoire
     */
    @Bean
    MapReactiveUserDetailsService users(PasswordEncoder enc) {
        return new MapReactiveUserDetailsService(
                User.withUsername(appUser).password(enc.encode(appPass)).roles("USER").build()
        );
    }

    // Après déconnexion, retour à la page d'accueil.
    private ServerLogoutSuccessHandler deconnexionVersAccueil() {
        RedirectServerLogoutSuccessHandler handler = new RedirectServerLogoutSuccessHandler();
        handler.setLogoutSuccessUrl(URI.create("/"));
        return handler;
    }
}
