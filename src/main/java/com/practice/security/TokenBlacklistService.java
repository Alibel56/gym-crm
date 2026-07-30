package com.practice.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Date expiration) {
        revokedTokens.put(tokenId, expiration.toInstant());
        cleanupExpired();
    }

    public boolean isRevoked(String tokenId) {
        Instant expiry = revokedTokens.get(tokenId);
        if (expiry == null) return false;
        if (Instant.now().isAfter(expiry)) {
            revokedTokens.remove(tokenId);
            return false;
        }
        return true;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}
