package com.practice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BruteForceProtectionServiceTest {

    private BruteForceProtectionService service;

    @BeforeEach
    void setUp() {
        service = new BruteForceProtectionService();
    }

    @Test
    void shouldNotBlockUserInitially() {
        assertThat(service.isBlocked("john")).isFalse();
        assertThat(service.remainingBlockSeconds("john")).isZero();
    }

    @Test
    void shouldBlockUserAfterThreeFailedAttempts() {
        service.loginFailed("john");
        service.loginFailed("john");

        assertThat(service.isBlocked("john")).isFalse();

        service.loginFailed("john");

        assertThat(service.isBlocked("john")).isTrue();
        assertThat(service.remainingBlockSeconds("john"))
                .isGreaterThan(0);
    }

    @Test
    void loginSucceededShouldClearFailedAttempts() {
        service.loginFailed("john");
        service.loginFailed("john");
        service.loginSucceeded("john");

        assertThat(service.isBlocked("john")).isFalse();

        service.loginFailed("john");
        service.loginFailed("john");

        assertThat(service.isBlocked("john")).isFalse();
    }

    @Test
    void usernameShouldBeCaseInsensitive() {
        service.loginFailed("John");
        service.loginFailed(" john ");
        service.loginFailed("JOHN");

        assertThat(service.isBlocked("john")).isTrue();
        assertThat(service.isBlocked("JOHN")).isTrue();
        assertThat(service.isBlocked(" John ")).isTrue();
    }
}