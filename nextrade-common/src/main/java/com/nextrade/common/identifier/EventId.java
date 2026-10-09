package com.nextrade.common.identifier;
import java.util.UUID;
public record EventId(UUID value) implements Identifier<EventId> {
    public EventId { if (value == null) throw new IllegalArgumentException("EventId cannot be null"); }
    public static EventId generate() { return new EventId(UUID.randomUUID()); }
    public static EventId of(UUID value) { return new EventId(value); }
    public static EventId parse(String value) { return of(UUID.fromString(value)); }
}
