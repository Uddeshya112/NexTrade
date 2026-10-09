package com.nextrade.common.identifier;
import java.util.UUID;
public record ReservationId(UUID value) implements Identifier<ReservationId> {
    public ReservationId { if (value == null) throw new IllegalArgumentException("ReservationId cannot be null"); }
    public static ReservationId generate() { return new ReservationId(UUID.randomUUID()); }
    public static ReservationId of(UUID value) { return new ReservationId(value); }
    public static ReservationId parse(String value) { return of(UUID.fromString(value)); }
}
