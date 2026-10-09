package com.nextrade.contracts.trading;

public record PortfolioResponse(String userId, String cashAvailable, String cashReserved, java.util.List<PortfolioHolding> holdings) {
}
