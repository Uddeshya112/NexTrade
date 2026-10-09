package com.nextrade.engine.matching;

import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.enumtype.UserRole;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookPropertyTest {

    @Test
    void priceTimePriorityInvariant() {
        Instrument instrument = createInstrument();
        User buyer = User.register("buyer", "buyer@example.com", "hash", UserRole.TRADER);
        User seller = User.register("seller", "seller@example.com", "hash", UserRole.TRADER);
        OrderBook book = new OrderBook(instrument.id(), instrument.tickSize(), null);
        List<Order> orders = new ArrayList<>();

        for (int command = 1; command <= 100; command++) {
            OrderSide side = command % 2 == 0 ? OrderSide.BUY : OrderSide.SELL;
            User user = side == OrderSide.BUY ? buyer : seller;
            BigDecimal value = BigDecimal.valueOf(100)
                .add(instrument.tickSize().value().multiply(BigDecimal.valueOf(command)));
            Order order = Order.limit(
                user,
                instrument,
                side,
                Quantity.of("1"),
                Price.of(value, instrument.tickSize().value()),
                TimeInForce.GTC,
                "property-" + command
            );
            orders.add(order);
            book.process(order);

            OrderBookSnapshot snapshot = book.snapshot(100);
            if (snapshot.bestBid().isPresent() && snapshot.bestAsk().isPresent()) {
                assertTrue(snapshot.bestBid().orElseThrow()
                    .compareTo(snapshot.bestAsk().orElseThrow()) <= 0);
            }
        }

        for (Order order : orders) {
            assertTrue(order.filledQuantity().compareTo(order.originalQuantity()) <= 0);
            assertEquals(order.workingQuantity(), order.remaining().add(order.filledQuantity()));
        }
    }

    private Instrument createInstrument() {
        return Instrument.create(
            "NXT",
            "NexTrade",
            "NXT",
            "USD",
            1,
            new BigDecimal("0.05")
        );
    }
}
