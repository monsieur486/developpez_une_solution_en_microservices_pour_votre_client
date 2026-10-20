package com.mr486.commun.configuration;

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
 * Sécurise les API REST des microservices back par authentification HTTP Basic.
 *
 * <p>Un seul utilisateur, défini en mémoire à partir des propriétés
 * {@code security.app-user.username} et {@code security.app-user.password}, suffit au besoin :
 * ni inscription ni gestion de droits ne sont demandées. Seuls les points de santé de
 * l'actuator restent publics, pour les contrôles de Docker. La classe n'est pas détectée
 * automatiquement : chaque microservice l'active par {@code @Import(SecuriteApiConfiguration.class)}.</p>
 *
 * <p><b>Exemple :</b> {@code GET /patients} sans en-tête {@code Authorization} renvoie 401 ;
 * avec {@code Authorization: Basic …} valide, la requête est traitée.</p>
 */
@Configuration
@EnableWebSecurity
public class SecuriteApiConfiguration {
    private static final String[] CHEMINS_PUBLICS = {"/actuator/**"};

    @Value("${security.app-user.username}")
    private String utilisateur;

    @Value("${security.app-user.password}")
    private String motDePasse;

    /**
     * Construit la chaîne de filtres : CSRF désactivé, Basic Auth exigé hors actuator.
     *
     * <p>CSRF protège les sessions portées par cookie ; une API sans état authentifiée à chaque
     * requête par en-tête n'en a pas besoin.</p>
     *
     * <p><b>Exemple :</b> {@code GET /actuator/health} répond sans authentification,
     * {@code GET /patients} exige des identifiants.</p>
     *
     * @param http configurateur de sécurité fourni par Spring
     * @return la chaîne de filtres appliquée à toutes les requêtes
     * @throws Exception si la configuration est invalide (signature imposée par {@link HttpSecurity#build()})
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Bean
    public SecurityFilterChain filtreDeSecurite(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requetes -> requetes
                        .requestMatchers(CHEMINS_PUBLICS).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Fournit l'encodeur des mots de passe : jamais de mot de passe stocké en clair.
     *
     * <p><b>Exemple :</b> {@code passwordEncoder().encode("secret")} produit une empreinte
     * BCrypt commençant par {@code $2a$}.</p>
     *
     * @return un encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Déclare l'unique utilisateur technique autorisé à appeler l'API.
     *
     * <p><b>Exemple :</b> avec {@code APP_USERNAME=app_user}, l'utilisateur {@code app_user}
     * reçoit le rôle {@code USER}.</p>
     *
     * @param encodeur encodeur utilisé pour chiffrer le mot de passe lu en configuration
     * @return le service d'utilisateurs en mémoire
     */
    @Bean
    public UserDetailsService utilisateurs(PasswordEncoder encodeur) {
        return new InMemoryUserDetailsManager(
                User.withUsername(utilisateur)
                        .password(encodeur.encode(motDePasse))
                        .roles("USER")
                        .build());
    }
}
