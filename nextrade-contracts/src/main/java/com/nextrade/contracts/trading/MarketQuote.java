package com.nextrade.contracts.trading;

public record MarketQuote(String instrumentId, String bid, String ask, String last, String timestamp) {
}
