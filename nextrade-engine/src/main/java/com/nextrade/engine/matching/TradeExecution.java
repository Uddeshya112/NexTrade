package com.nextrade.engine.matching;

import com.nextrade.common.enumtype.OrderType;
import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.identifier.OrderId;
import com.nextrade.common.identifier.TradeId;
import com.nextrade.common.identifier.UserId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.order.Trade;

import java.time.Instant;

public record TradeExecution(
        TradeId tradeId,
        OrderId buyOrderId,
        OrderId sellOrderId,
        UserId buyer,
        UserId seller,
        InstrumentId instrumentId,
        Quantity quantity,
        Price price,
        OrderType aggressorType,
        long sequence,
        Instant executedAt,
        Order buyOrder,
        Order sellOrder) {
    public Trade toTrade() {
        return new Trade(tradeId, buyOrderId, sellOrderId, buyer, seller, instrumentId,
                quantity, price, executedAt, sequence);
    }
}
