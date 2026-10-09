package com.nextrade.common.identifier;
import java.util.UUID;
public record StrategyId(UUID value) implements Identifier<StrategyId> {
    public StrategyId { if (value == null) throw new IllegalArgumentException("StrategyId cannot be null"); }
    public static StrategyId generate() { return new StrategyId(UUID.randomUUID()); }
    public static StrategyId of(UUID value) { return new StrategyId(value); }
    public static StrategyId parse(String value) { return of(UUID.fromString(value)); }
}
