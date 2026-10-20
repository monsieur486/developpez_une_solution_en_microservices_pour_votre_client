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
 * Sécurise l'interface web par connexion via formulaire.
 *
 * <p>Un seul utilisateur en mémoire, au mot de passe chiffré en BCrypt, suffit au besoin : ni
 * inscription ni gestion de droits ne sont demandées. Seules l'accueil, la page de connexion et
 * les ressources statiques sont publiques.</p>
 *
 * <p><b>Exemple :</b> un visiteur non connecté qui ouvre {@code /app/patients} est redirigé vers
 * {@code /login}, puis ramené sur la liste des patients après connexion.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private static final String[] PAGES_PUBLIQUES = {"/", "/home", "/webjars/**", "/favicon.*"};

    /**
     * Construit la chaîne de filtres : pages publiques, connexion par formulaire, déconnexion.
     *
     * <p>La protection CSRF reste active : les formulaires Thymeleaf ({@code th:action}) ajoutent
     * automatiquement le jeton attendu.</p>
     *
     * <p><b>Exemple :</b> {@code POST /logout} ferme la session et renvoie vers l'accueil.</p>
     *
     * @param http configurateur de sécurité fourni par Spring
     * @return la chaîne de filtres appliquée à toutes les requêtes
     * @throws Exception si la configuration est invalide (signature imposée par {@link HttpSecurity#build()})
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Bean
    public SecurityFilterChain filtreDeSecurite(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requetes -> requetes
                        .requestMatchers(PAGES_PUBLIQUES).permitAll()
                        .anyRequest().authenticated())
                .formLogin(formulaire -> formulaire
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/app/patients", true)
                        .permitAll())
                .logout(deconnexion -> deconnexion
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .permitAll());
        return http.build();
    }

    /**
     * Fournit l'encodeur des mots de passe : jamais de mot de passe conservé en clair.
     *
     * <p><b>Exemple :</b> {@code passwordEncoder().encode("secret")} produit une empreinte BCrypt
     * commençant par {@code $2a$}.</p>
     *
     * @return un encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Déclare l'unique praticien autorisé à se connecter.
     *
     * <p><b>Exemple :</b> avec {@code APP_USERNAME=app_user}, l'utilisateur {@code app_user} reçoit
     * le rôle {@code USER}.</p>
     *
     * @param encodeur    encodeur utilisé pour chiffrer le mot de passe
     * @param utilisateur identifiant de connexion lu en configuration
     * @param motDePasse  mot de passe lu en configuration
     * @return le service d'utilisateurs en mémoire
     */
    @Bean
    public UserDetailsService utilisateurs(
            PasswordEncoder encodeur,
            @Value("${app.auth.username}") String utilisateur,
            @Value("${app.auth.password}") String motDePasse) {
        return new InMemoryUserDetailsManager(
                User.withUsername(utilisateur)
                        .password(encodeur.encode(motDePasse))
                        .roles("USER")
                        .build());
    }
}
