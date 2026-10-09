package com.nextrade.client.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class RateLimiter {
    private static final RateLimiter INSTANCE = new RateLimiter();
    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    private RateLimiter() {}

    public static RateLimiter getInstance() { return INSTANCE; }

    public record AttemptRecord(int count, Instant firstAttempt, Instant lastAttempt) {}

    public boolean tryAcquire(String key, int maxAttempts, long windowMinutes) {
        AttemptRecord record = attempts.get(key);
        Instant now = Instant.now();

        if (record == null) {
            attempts.put(key, new AttemptRecord(1, now, now));
            return true;
        }

        if (record.firstAttempt().plus(windowMinutes, java.time.temporal.ChronoUnit.MINUTES).isBefore(now)) {
            attempts.put(key, new AttemptRecord(1, now, now));
            return true;
        }

        if (record.count() >= maxAttempts) return false;

        attempts.put(key, new AttemptRecord(record.count() + 1, record.firstAttempt(), now));
        return true;
    }

    public void reset(String key) { attempts.remove(key); }
    public long getRemainingTime(String key, long windowMinutes) {
        AttemptRecord record = attempts.get(key);
        if (record == null) return 0;
        Instant expiry = record.firstAttempt().plus(windowMinutes, java.time.temporal.ChronoUnit.MINUTES);
        return java.time.Duration.between(Instant.now(), expiry).toSeconds();
    }
}
