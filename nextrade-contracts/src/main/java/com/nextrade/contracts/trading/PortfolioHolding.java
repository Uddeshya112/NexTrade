package com.nextrade.contracts.trading;

public record PortfolioHolding(String instrumentId, String quantity, String averagePrice, String marketPrice, String unrealizedPnl) {
}
