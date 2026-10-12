package com.nextrade.service;

import com.nextrade.common.identifier.OrderId;
import com.nextrade.common.identifier.UserId;
import com.nextrade.contracts.trading.TradeResponse;
import com.nextrade.domain.order.Trade;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.domain.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExecutionQueryService {
    private final OrderRepository orders;
    private final TradeRepository trades;

    public ExecutionQueryService(OrderRepository orders, TradeRepository trades) {
        this.orders = orders;
        this.trades = trades;
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getExecutions(UUID orderUuid, UserId userId) {
        OrderId orderId = OrderId.of(orderUuid);
        var order = orders.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.user().id().equals(userId)) throw new SecurityException("Order belongs to another user");
        return trades.findByOrder(orderId).stream().map(trade -> toResponse(orderId, order.side().name(), trade)).toList();
    }

    private static TradeResponse toResponse(OrderId orderId, String side, Trade trade) {
        return new TradeResponse(trade.id().value().toString(), orderId.value().toString(), side,
                trade.quantity().toString(), trade.price().value().toPlainString(), trade.executedAt().toString());
    }
}
