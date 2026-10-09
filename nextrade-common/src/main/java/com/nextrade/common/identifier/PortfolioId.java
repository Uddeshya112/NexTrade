package com.nextrade.common.identifier;
import java.util.UUID;
public record PortfolioId(UUID value) implements Identifier<PortfolioId> {
    public PortfolioId { if (value == null) throw new IllegalArgumentException("PortfolioId cannot be null"); }
    public static PortfolioId generate() { return new PortfolioId(UUID.randomUUID()); }
    public static PortfolioId of(UUID value) { return new PortfolioId(value); }
    public static PortfolioId parse(String value) { return of(UUID.fromString(value)); }
}
