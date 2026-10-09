package com.nextrade.contracts.common;

public record ApiError(String code, String message, String correlationId, String timestamp) {
}
