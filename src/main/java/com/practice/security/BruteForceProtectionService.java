package com.practice.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        if (username == null) return false;
        LoginAttempt attempt = attempts.get(normalize(username));
        if (attempt == null || attempt.blockedUntil() == null) return false;
        if (Instant.now().isAfter(attempt.blockedUntil())) {
            attempts.remove(normalize(username));
            return false;
        }
        return true;
    }

    public long remainingBlockSeconds(String username) {
        LoginAttempt attempt = attempts.get(normalize(username));
        if (attempt == null || attempt.blockedUntil() == null) return 0;
        return Math.max(0, Duration.between(Instant.now(), attempt.blockedUntil()).toSeconds());
    }

    public void loginSucceeded(String username) {
        attempts.remove(normalize(username));
    }

    public void loginFailed(String username) {
        String key = normalize(username);
        attempts.compute(key, (ignored, current) -> {
            int failures = current == null ? 1 : current.failures() + 1;
            Instant blockedUntil = failures >= MAX_ATTEMPTS
                    ? Instant.now().plus(BLOCK_DURATION)
                    : null;
            return new LoginAttempt(failures, blockedUntil);
        });
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private record LoginAttempt(int failures, Instant blockedUntil) {}
}
