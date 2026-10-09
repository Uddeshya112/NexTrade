package com.nextrade.common.identifier;
import java.util.UUID;
public record OrderId(UUID value) implements Identifier<OrderId> {
    public OrderId { if (value == null) throw new IllegalArgumentException("OrderId cannot be null"); }
    public static OrderId generate() { return new OrderId(UUID.randomUUID()); }
    public static OrderId of(UUID value) { return new OrderId(value); }
    public static OrderId parse(String value) { return of(UUID.fromString(value)); }
}
