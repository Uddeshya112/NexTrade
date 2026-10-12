package com.nextrade.contracts.trading;

/** Public quote contract. Prices are decimal strings so clients do not lose precision. */
public record MarketQuote(String instrumentId, String bid, String ask, String last,
                          String mode, String source, String asOf) { }
