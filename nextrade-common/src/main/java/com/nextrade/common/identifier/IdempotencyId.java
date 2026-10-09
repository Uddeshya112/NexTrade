package com.nextrade.common.identifier;
import java.util.UUID;
public record IdempotencyId(UUID value) implements Identifier<IdempotencyId> {
    public IdempotencyId { if (value == null) throw new IllegalArgumentException("IdempotencyId cannot be null"); }
    public static IdempotencyId generate() { return new IdempotencyId(UUID.randomUUID()); }
    public static IdempotencyId of(UUID value) { return new IdempotencyId(value); }
    public static IdempotencyId parse(String value) { return of(UUID.fromString(value)); }
}
