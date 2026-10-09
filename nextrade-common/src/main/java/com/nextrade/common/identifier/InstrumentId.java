package com.nextrade.common.identifier;
import java.util.UUID;
public record InstrumentId(UUID value) implements Identifier<InstrumentId> {
    public InstrumentId { if (value == null) throw new IllegalArgumentException("InstrumentId cannot be null"); }
    public static InstrumentId generate() { return new InstrumentId(UUID.randomUUID()); }
    public static InstrumentId of(UUID value) { return new InstrumentId(value); }
    public static InstrumentId parse(String value) { return of(UUID.fromString(value)); }
}
