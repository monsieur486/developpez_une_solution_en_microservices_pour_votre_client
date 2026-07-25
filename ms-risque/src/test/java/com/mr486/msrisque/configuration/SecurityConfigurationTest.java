package com.mr486.msrisque.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationTest {

    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void passwordEncoder_encodeEtVerifieUnMotDePasse() {
        PasswordEncoder encoder = configuration.passwordEncoder();

        String hache = encoder.encode("pasW0rd");

        assertThat(encoder.matches("pasW0rd", hache)).isTrue();
    }

    @Test
    void users_declareLUtilisateurApplicatifAvecLeRoleAdmin() {
        ReflectionTestUtils.setField(configuration, "appUser", "ADMIN");
        ReflectionTestUtils.setField(configuration, "appPass", "pasW0rd");

        MapReactiveUserDetailsService service = configuration.users(configuration.passwordEncoder());
        UserDetails utilisateur = service.findByUsername("ADMIN").block();

        assertThat(utilisateur).isNotNull();

        assertThat(utilisateur.getUsername()).isEqualTo("ADMIN");
        assertThat(utilisateur.getAuthorities()).anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
