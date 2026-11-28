package com.example.psp_backend.common.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long TIME_WINDOW_MS = 60_000; // 1 minut

    private final ConcurrentHashMap<String, LoginRateLimiter.Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        LoginRateLimiter.Attempt attempt = attempts.get(ip);

        if (attempt == null) {
            return false;
        }

        long now = Instant.now().toEpochMilli();

        // Ako je prozor istekao – reset
        if (now - attempt.firstAttemptTime > TIME_WINDOW_MS) {
            attempts.remove(ip);
            return false;
        }

        return attempt.count >= MAX_ATTEMPTS;
    }

    public void loginFailed(String ip) {
        long now = Instant.now().toEpochMilli();

        attempts.compute(ip, (key, attempt) -> {
            if (attempt == null || now - attempt.firstAttemptTime > TIME_WINDOW_MS) {
                return new LoginRateLimiter.Attempt(1, now);
            }
            attempt.count++;
            return attempt;
        });
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
    }

    private static class Attempt {
        int count;
        long firstAttemptTime;

        Attempt(int count, long firstAttemptTime) {
            this.count = count;
            this.firstAttemptTime = firstAttemptTime;
        }
    }
}
