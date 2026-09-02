package com.practice.security;

import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBlacklistServiceTest {

    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new TokenBlacklistService();
    }

    @Test
    @NegativeTest
    void shouldNotConsiderUnknownTokenRevoked() {
        assertThat(service.isRevoked("token-id")).isFalse();
    }

    @Test
    @PositiveTest
    void shouldRevokeToken() {
        String tokenId = "token-id";
        Date expiration = Date.from(Instant.now().plusSeconds(60));

        service.revoke(tokenId, expiration);

        assertThat(service.isRevoked(tokenId)).isTrue();
    }

    @Test
    @PositiveTest
    void shouldRemoveExpiredToken() {
        String tokenId = "token-id";
        Date expiration = Date.from(Instant.now().minusSeconds(1));

        service.revoke(tokenId, expiration);

        assertThat(service.isRevoked(tokenId)).isFalse();
    }

    @Test
    @PositiveTest
    void shouldKeepOnlyValidTokensAfterCleanup() {
        service.revoke(
                "expired",
                Date.from(Instant.now().minusSeconds(1))
        );

        service.revoke(
                "valid",
                Date.from(Instant.now().plusSeconds(60))
        );

        assertThat(service.isRevoked("expired")).isFalse();
        assertThat(service.isRevoked("valid")).isTrue();
    }
}
