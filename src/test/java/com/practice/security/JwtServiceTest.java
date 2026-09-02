package com.practice.security;

import com.practice.testtags.ExceptionTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        String rawSecret = "this-is-a-very-long-secret-key-for-jwt-testing-123456";
        String base64Secret = Base64.getEncoder()
                .encodeToString(rawSecret.getBytes(StandardCharsets.UTF_8));

        jwtService = new JwtService(base64Secret, 3600);

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john")
                .password("encoded-password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    @PositiveTest
    void generateTokenShouldCreateValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("john");
        assertThat(jwtService.extractTokenId(token)).isNotBlank();
        assertThat(jwtService.extractExpiration(token)).isAfter(new Date());
        assertThat(jwtService.isValid(token, userDetails)).isTrue();
    }

    @Test
    @PositiveTest
    void isValidShouldReturnFalseForDifferentUsername() {
        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = org.springframework.security.core.userdetails.User
                .withUsername("alex")
                .password("encoded-password")
                .authorities("ROLE_USER")
                .build();

        assertThat(jwtService.isValid(token, anotherUser)).isFalse();
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void expiredTokenShouldThrowJwtException() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(
                Base64.getEncoder().encodeToString(
                        "this-is-a-very-long-secret-key-for-expired-test-123456"
                                .getBytes(StandardCharsets.UTF_8)
                ),
                1
        );

        String token = shortLivedJwtService.generateToken(userDetails);

        Thread.sleep(1100);

        assertThatThrownBy(() -> shortLivedJwtService.extractUsername(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @PositiveTest
    void tokenSignedWithAnotherKeyShouldBeRejected() {
        String token = jwtService.generateToken(userDetails);

        JwtService anotherJwtService = new JwtService(
                Base64.getEncoder().encodeToString(
                        "another-very-long-secret-key-for-jwt-testing-123456"
                                .getBytes(StandardCharsets.UTF_8)
                ),
                3600
        );

        assertThatThrownBy(() -> anotherJwtService.extractUsername(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @PositiveTest
    void getExpirationSecondsShouldReturnConfiguredValue() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600);
    }
}
