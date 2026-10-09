package com.nextrade.contracts.trading;

public record OrderResponse(String orderId, String status, String message, String revision) {
}
