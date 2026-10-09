package com.nextrade.contracts.trading;

public record OrderRequest(String clientOrderId, String instrumentId, String side, String type, String quantity, String limitPrice, String stopPrice, String timeInForce) {
}
