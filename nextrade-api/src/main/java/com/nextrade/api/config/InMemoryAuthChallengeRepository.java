package com.nextrade.api.config;

import com.nextrade.common.identifier.UserId;
import com.nextrade.service.ports.AuthChallengeRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemoryAuthChallengeRepository implements AuthChallengeRepository {
    private record Verify(UserId user, String digest, Instant expiresAt, AtomicInteger attempts,
                          int maxAttempts, AtomicBoolean used) { }
    private record Reset(UserId user, Instant expiresAt, AtomicBoolean used) { }

    private final ConcurrentHashMap<String, Verify> verifications = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Reset> resets = new ConcurrentHashMap<>();

    @Override
    public void createVerification(UserId userId, String selector, String digest, Instant expiresAt, int maxAttempts) {
        verifications.put(selector, new Verify(userId, digest, expiresAt, new AtomicInteger(), maxAttempts, new AtomicBoolean()));
    }

    @Override
    public UserId consumeVerification(String selector, String digest, Instant now, int maxAttempts) {
        Verify value = verifications.get(selector);
        if (value == null || value.used().get() || !value.expiresAt().isAfter(now)
                || value.attempts().get() >= Math.min(maxAttempts, value.maxAttempts())) return null;
        if (!Objects.equals(value.digest(), digest)) {
            value.attempts().incrementAndGet();
            return null;
        }
        return value.used().compareAndSet(false, true) ? value.user() : null;
    }

    @Override
    public void createReset(UserId userId, String digest, Instant expiresAt) {
        resets.put(digest, new Reset(userId, expiresAt, new AtomicBoolean()));
    }

    @Override
    public UserId consumeReset(String digest, Instant now) {
        Reset value = resets.get(digest);
        if (value == null || value.used().get() || !value.expiresAt().isAfter(now)) return null;
        return value.used().compareAndSet(false, true) ? value.user() : null;
    }

    @Override
    public boolean isResetValid(String digest, Instant now) {
        Reset value = resets.get(digest);
        return value != null && !value.used().get() && value.expiresAt().isAfter(now);
    }
}
