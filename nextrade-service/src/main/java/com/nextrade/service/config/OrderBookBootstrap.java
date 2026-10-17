package com.nextrade.service.config;

import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.enumtype.OrderType;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.service.MarketDataService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Restores persisted active orders before the API begins serving requests. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public final class OrderBookBootstrap implements ApplicationRunner {
    private final InstrumentRepository instruments;
    private final OrderRepository orders;
    private final MatchingEngine matching;
    private final MarketDataService marketData;

    public OrderBookBootstrap(InstrumentRepository instruments, OrderRepository orders,
                              MatchingEngine matching, MarketDataService marketData) {
        this.instruments = instruments;
        this.orders = orders;
        this.matching = matching;
        this.marketData = marketData;
    }

    @Override
    public void run(ApplicationArguments args) {
        instruments.findActive().forEach(instrument -> {
            orders.findOpenByInstrument(instrument.id()).stream()
                    .sorted(java.util.Comparator.comparingLong(order -> order.sequence()))
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
}
