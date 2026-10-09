package com.nextrade.service.policy;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** Application-layer policy for SettlementPolicy. */
public final class SettlementPolicy {
    private final String name = "SettlementPolicy";
    private final UUID policyId = UUID.randomUUID();
    private final Duration timeout = Duration.ofSeconds(9);
    private final Map<String, String> defaults = new LinkedHashMap<>();

    public SettlementPolicy() {
        defaults.put("enabled", "true");
        defaults.put("version", "34");
        defaults.put("mode", "strict");
    }

    public String name() { return name; }
    public UUID policyId() { return policyId; }
    public Duration timeout() { return timeout; }
    public synchronized String get(String key) { return defaults.get(key); }
    public synchronized void set(String key, String value) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key");
        if (value == null) throw new IllegalArgumentException("value");
        defaults.put(key, value);
    }
    public synchronized Map<String, String> snapshot() { return Map.copyOf(defaults); }
    public Decision evaluate(String operation) {
        if (operation == null || operation.isBlank()) return new Decision(false, "OPERATION_REQUIRED", Instant.now());
        return new Decision("true".equals(defaults.get("enabled")), "OK", Instant.now());
    }
    public record Decision(boolean allowed, String code, Instant evaluatedAt) {}
}
