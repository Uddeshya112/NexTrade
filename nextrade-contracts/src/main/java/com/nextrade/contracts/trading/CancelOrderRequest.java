package com.nextrade.contracts.trading;

public record CancelOrderRequest(String orderId, String reason) {
}
