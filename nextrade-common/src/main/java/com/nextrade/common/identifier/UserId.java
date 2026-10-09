package com.nextrade.common.identifier;
import java.util.UUID;
public record UserId(UUID value) implements Identifier<UserId> {
    public UserId { if (value == null) throw new IllegalArgumentException("UserId cannot be null"); }
    public static UserId generate() { return new UserId(UUID.randomUUID()); }
    public static UserId of(UUID value) { return new UserId(value); }
    public static UserId parse(String value) { return of(UUID.fromString(value)); }
}
