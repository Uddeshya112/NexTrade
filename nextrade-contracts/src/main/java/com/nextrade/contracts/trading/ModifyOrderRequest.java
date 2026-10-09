package com.nextrade.contracts.trading;

public record ModifyOrderRequest(String orderId, String quantity, String limitPrice) {
}
