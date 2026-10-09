package com.nextrade.common.identifier;
import java.util.UUID;
public record LedgerEntryId(UUID value) implements Identifier<LedgerEntryId> {
    public LedgerEntryId { if (value == null) throw new IllegalArgumentException("LedgerEntryId cannot be null"); }
    public static LedgerEntryId generate() { return new LedgerEntryId(UUID.randomUUID()); }
    public static LedgerEntryId of(UUID value) { return new LedgerEntryId(value); }
    public static LedgerEntryId parse(String value) { return of(UUID.fromString(value)); }
}
