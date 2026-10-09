package com.nextrade.service.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryAcquire(String key, int maxAttempts, Duration window) {
        String redisKey = "ratelimit:" + key;
        Long current = stringRedisTemplate.opsForValue().increment(redisKey);
        
        if (current == 1) {
            stringRedisTemplate.expire(redisKey, window);
        }
        
        return current <= maxAttempts;
    }

    public long getRemainingTime(String key) {
        String redisKey = "ratelimit:" + key;
        return stringRedisTemplate.getExpire(redisKey, java.util.concurrent.TimeUnit.SECONDS);
    }

    public void reset(String key) {
        stringRedisTemplate.delete("ratelimit:" + key);
    }

    public boolean tryLogin(String identifier) {
        return tryAcquire("login:" + identifier.toLowerCase(), 5, java.time.Duration.ofMinutes(15));
    }

    public boolean tryForgotPassword(String email) {
        return tryAcquire("forgot:" + email.toLowerCase(), 3, Duration.ofHours(1));
    }

    public boolean tryVerification(String identifier) {
        return tryAcquire("verify:" + identifier.toLowerCase(), 5, Duration.ofHours(1));
    }
}
