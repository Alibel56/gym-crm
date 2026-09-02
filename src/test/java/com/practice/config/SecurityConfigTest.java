package com.practice.config;

import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Test
    @PositiveTest
    void passwordEncoderShouldEncodeAndMatchPassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String rawPassword = "Password123";
        String encodedPassword = encoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(encoder.matches("WrongPassword", encodedPassword)).isFalse();
    }

    @Test
    @PositiveTest
    void corsConfigurationShouldBeConfiguredCorrectly() {
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource(
                        "http://localhost:3000,http://localhost:4200"
                );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/trainers");

        CorsConfiguration configuration =
                source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();

        assertThat(configuration.getAllowedOrigins())
                .containsExactly(
                        "http://localhost:3000",
                        "http://localhost:4200"
                );

        assertThat(configuration.getAllowedMethods())
                .containsExactly(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                );

        assertThat(configuration.getAllowedHeaders())
                .containsExactly(
                        "Authorization",
                        "Content-Type",
                        "X-Transaction-Id"
                );

        assertThat(configuration.getExposedHeaders())
                .containsExactly("X-Transaction-Id");

        assertThat(configuration.getAllowCredentials()).isTrue();
        assertThat(configuration.getMaxAge()).isEqualTo(3600L);
    }
}
