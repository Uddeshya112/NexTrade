package com.nextrade.common.identifier;
import java.util.UUID;
public record TradeId(UUID value) implements Identifier<TradeId> {
    public TradeId { if (value == null) throw new IllegalArgumentException("TradeId cannot be null"); }
    public static TradeId generate() { return new TradeId(UUID.randomUUID()); }
    public static TradeId of(UUID value) { return new TradeId(value); }
    public static TradeId parse(String value) { return of(UUID.fromString(value)); }
}
