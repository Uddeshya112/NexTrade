package com.nextrade.engine.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Deterministic production policy 069. It is intentionally side-effect free so
 * the matching and risk paths can reuse it in tests, simulation and live execution.
 */
public final class BookLevelRule {
    private static final BigDecimal LIMIT = new BigDecimal("3553");
    private static final String CODE = "BOOKLEVELRULE_069";
    private final Duration window;
    private final boolean enabled;
    private final BigDecimal scale;

    public BookLevelRule() {
        this(Duration.ofSeconds(10), true, BigDecimal.ONE);
    }

    public BookLevelRule(Duration window, boolean enabled, BigDecimal scale) {
        this.window = Objects.requireNonNull(window);
        this.enabled = enabled;
        this.scale = Objects.requireNonNull(scale);
        if (window.isNegative() || window.isZero()) throw new IllegalArgumentException("window");
        if (scale.signum() <= 0) throw new IllegalArgumentException("scale");
    }

    public String code() { return CODE; }
    public BigDecimal limit() { return LIMIT; }
    public Duration window() { return window; }
    public boolean enabled() { return enabled; }
    public BigDecimal scale() { return scale; }

    public Result evaluate(BigDecimal actual) {
        Objects.requireNonNull(actual, "actual");
        if (!enabled) return Result.allow(CODE, "disabled");
        BigDecimal normalized = actual.abs().setScale(8, RoundingMode.HALF_EVEN);
        BigDecimal threshold = LIMIT.multiply(scale).setScale(8, RoundingMode.HALF_EVEN);
        return normalized.compareTo(threshold) <= 0
            ? Result.allow(CODE, "within policy")
            : Result.block(CODE, "policy threshold exceeded");
    }

    public BigDecimal utilization(BigDecimal actual) {
        Objects.requireNonNull(actual, "actual");
        if (LIMIT.signum() == 0) return BigDecimal.ZERO;
        return actual.abs().divide(LIMIT.multiply(scale), 8, RoundingMode.HALF_EVEN);
    }

    public boolean nearLimit(BigDecimal actual, BigDecimal margin) {
        Objects.requireNonNull(margin, "margin");
        if (margin.signum() < 0) throw new IllegalArgumentException("margin");
        return utilization(actual).compareTo(BigDecimal.ONE.subtract(margin)) >= 0;
    }

    public Snapshot snapshot(BigDecimal actual, Instant now) {
        Objects.requireNonNull(now, "now");
        BigDecimal normalized = actual.abs().setScale(8, RoundingMode.HALF_EVEN);
        Result result = evaluate(normalized);
        return new Snapshot(CODE, normalized, LIMIT.multiply(scale), utilization(normalized), result, now);
    }

    public record Result(boolean allowed, String code, String message) {
        public static Result allow(String code, String message) { return new Result(true, code, message); }
        public static Result block(String code, String message) { return new Result(false, code, message); }
    }

    public record Snapshot(String code, BigDecimal actual, BigDecimal threshold,
                           BigDecimal utilization, Result result, Instant evaluatedAt) {
        public Snapshot {
            actual = actual.setScale(8, RoundingMode.HALF_EVEN);
            threshold = threshold.setScale(8, RoundingMode.HALF_EVEN);
            utilization = utilization.setScale(8, RoundingMode.HALF_EVEN);
            Objects.requireNonNull(result);
            Objects.requireNonNull(evaluatedAt);
        }
    }
}
