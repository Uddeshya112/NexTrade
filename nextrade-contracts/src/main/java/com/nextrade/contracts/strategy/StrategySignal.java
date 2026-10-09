package com.nextrade.contracts.strategy;

public record StrategySignal(String strategyId, String instrumentId, String action, String quantity, String reason) {
}
