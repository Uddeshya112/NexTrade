package com.nextrade.service.config;

import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.domain.repository.TradeRepository;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.enumtype.OrderType;
import com.nextrade.common.enumtype.OrderStatus;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.service.MarketDataService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Restores persisted active orders before the API begins serving requests. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public final class OrderBookBootstrap implements ApplicationRunner {
    private final InstrumentRepository instruments;
    private final OrderRepository orders;
    private final TradeRepository trades;
    private final MatchingEngine matching;
    private final MarketDataService marketData;

    public OrderBookBootstrap(InstrumentRepository instruments, OrderRepository orders, TradeRepository trades,
                              MatchingEngine matching, MarketDataService marketData) {
        this.instruments = instruments;
        this.orders = orders;
        this.trades = trades;
        this.matching = matching;
        this.marketData = marketData;
    }

    @Override
    public void run(ApplicationArguments args) {
        instruments.findActive().forEach(instrument -> {
            orders.findOpenByInstrument(instrument.id()).stream()
                    .sorted(java.util.Comparator.comparingLong(order -> order.sequence()))
                    .map(this::reconcileFills)
                    .forEach(order -> {
                        if (order.type() == OrderType.MARKET || order.tif() == TimeInForce.IOC || order.tif() == TimeInForce.FOK) {
                            matching.place(order).join();
                            orders.save(order);
                        } else {
                            matching.restore(order).join();
                        }
                    });
            var lastPrice = marketData.getCurrentPrice(instrument.id());
            if (lastPrice != null) matching.seedLastTradePrice(instrument.id(), lastPrice).join();
        });
    }

    private com.nextrade.domain.order.Order reconcileFills(com.nextrade.domain.order.Order order) {
        var executions = trades.findByOrder(order.id());
        BigDecimal filled = BigDecimal.ZERO;
        BigDecimal notional = BigDecimal.ZERO;
        for (var execution : executions) {
            filled = filled.add(execution.quantity().value());
            notional = notional.add(execution.quantity().value().multiply(execution.price().value()));
        }
        Quantity filledQuantity = new Quantity(filled);
        if (filledQuantity.compareTo(order.workingQuantity()) > 0) {
            throw new IllegalStateException("Persisted executions exceed order quantity " + order.id());
        }
        Price average = filled.signum() == 0 ? null : Price.of(
                notional.divide(filled, 8, RoundingMode.HALF_EVEN), order.instrument().tickSize().tickSize());
        OrderStatus reconciledStatus = filled.compareTo(order.workingQuantity().value()) >= 0
                ? OrderStatus.FILLED : filled.signum() > 0 ? OrderStatus.PARTIALLY_FILLED : order.status();
        if (filledQuantity.compareTo(order.filledQuantity()) == 0
                && reconciledStatus == order.status()
                && (average == null ? order.avgFillPrice() == null
                : order.avgFillPrice() != null && average.compareTo(order.avgFillPrice()) == 0)) {
            return order;
        }
        var corrected = com.nextrade.domain.order.Order.reconstitute(order.id(), order.user(), order.instrument(),
                order.side(), order.type(), order.originalQuantity(), order.workingQuantity(), filledQuantity,
                order.limitPrice(), order.stopPrice(), average, order.tif(), order.clientOrderId(), reconciledStatus,
                order.expiresAt(), order.triggeredAt(), order.sequence(), order.createdAt(), order.updatedAt(), order.revision());
        return orders.save(corrected);
    }
}
