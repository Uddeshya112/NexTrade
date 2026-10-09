package com.nextrade.common.identifier;
import java.util.UUID;
public record WalletId(UUID value) implements Identifier<WalletId> {
    public WalletId { if (value == null) throw new IllegalArgumentException("WalletId cannot be null"); }
    public static WalletId generate() { return new WalletId(UUID.randomUUID()); }
    public static WalletId of(UUID value) { return new WalletId(value); }
    public static WalletId parse(String value) { return of(UUID.fromString(value)); }
}
