package com.nextrade.common.identifier;
import java.util.UUID;
public record SessionId(UUID value) implements Identifier<SessionId> {
    public SessionId { if (value == null) throw new IllegalArgumentException("SessionId cannot be null"); }
    public static SessionId generate() { return new SessionId(UUID.randomUUID()); }
    public static SessionId of(UUID value) { return new SessionId(value); }
    public static SessionId parse(String value) { return of(UUID.fromString(value)); }
}
