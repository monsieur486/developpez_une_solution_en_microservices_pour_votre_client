package com.mr486.mspatients.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    void users_declareLUtilisateurApplicatifAvecLeRoleUser() {
        ReflectionTestUtils.setField(configuration, "appUser", "app_user");
        ReflectionTestUtils.setField(configuration, "appPass", "app_password");

        UserDetailsService service = configuration.users(configuration.passwordEncoder());
        UserDetails utilisateur = service.loadUserByUsername("app_user");

        assertThat(utilisateur.getUsername()).isEqualTo("app_user");
        assertThat(utilisateur.getAuthorities()).anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));
    }
}
