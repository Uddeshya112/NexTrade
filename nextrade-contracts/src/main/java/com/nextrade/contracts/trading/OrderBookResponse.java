package com.nextrade.contracts.trading;

public record OrderBookResponse(String instrumentId, java.util.List<OrderBookLevel> bids, java.util.List<OrderBookLevel> asks, String lastTradePrice) {
}
