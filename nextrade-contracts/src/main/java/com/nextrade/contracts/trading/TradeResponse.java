package com.nextrade.contracts.trading;

public record TradeResponse(String tradeId, String orderId, String side, String quantity, String price, String timestamp) {
}
