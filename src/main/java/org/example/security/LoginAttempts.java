package org.example.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttempts {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private record Attempt(int failures, Instant lockedUntil) {
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String username) {
        Attempt attempt = attempts.get(key(username));
        return attempt != null && attempt.lockedUntil() != null && Instant.now().isBefore(attempt.lockedUntil());
    }

    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        attempts.merge(key(event.getAuthentication().getName()), new Attempt(1, null), (old, first) -> {
            boolean lockExpired = old.lockedUntil() != null && !Instant.now().isBefore(old.lockedUntil());
            int failures = lockExpired ? 1 : old.failures() + 1;
            return new Attempt(failures, failures >= MAX_FAILURES ? Instant.now().plus(LOCK_TIME) : null);
        });
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        attempts.remove(key(event.getAuthentication().getName()));
    }

    private String key(String username) {
        return username == null ? "" : username.toLowerCase();
    }
}
