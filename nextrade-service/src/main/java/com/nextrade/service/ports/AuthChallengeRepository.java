package com.nextrade.service.ports;

import com.nextrade.common.identifier.UserId;

import java.time.Instant;

public interface AuthChallengeRepository {
    void createVerification(UserId userId, String selector, String digest, Instant expiresAt, int maxAttempts);
    UserId consumeVerification(String selector, String digest, Instant now, int maxAttempts);
    void createReset(UserId userId, String digest, Instant expiresAt);
    UserId consumeReset(String digest, Instant now);
    boolean isResetValid(String digest, Instant now);
}
