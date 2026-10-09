package com.nextrade.engine.matching;

import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.user.User;
import com.nextrade.common.enumtype.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookInvariantTest {
    private static final Price TICK = Price.of("0.01", "0.01");
    private static final Price PRICE = Price.of("100.00", "0.01");

    @Test
    void fokDoesNotCountOwnRestingLiquidityAsFillable() {
        Instrument instrument = instrument();
        User owner = user("owner");
        User other = user("other");
        OrderBook book = new OrderBook(instrument.id(), TICK, null);
        book.process(Order.limit(owner, instrument, OrderSide.SELL, Quantity.of("5"), PRICE, TimeInForce.GTC, "own-sell"));
        Order externalSell = Order.limit(other, instrument, OrderSide.SELL, Quantity.of("5"), PRICE, TimeInForce.GTC, "external-sell");
        book.process(externalSell);

        Order fokBuy = Order.limit(owner, instrument, OrderSide.BUY, Quantity.of("10"), PRICE, TimeInForce.FOK, "fok-buy");
        List<TradeExecution> trades = book.process(fokBuy);

        assertTrue(trades.isEmpty(), "FOK must be rejected before any partial execution");
        assertEquals(0, fokBuy.filledQuantity().compareTo(Quantity.zero()));
        assertTrue(fokBuy.status().isTerminal());
        assertTrue(externalSell.active(), "external liquidity must remain untouched when FOK cannot fully fill");
    }

    @Test
    void incomingSellSelfTradeCancelsRestingBuyNotIncomingSell() {
        Instrument instrument = instrument();
        User owner = user("same_owner");
        OrderBook book = new OrderBook(instrument.id(), TICK, null);
        Order restingBuy = Order.limit(owner, instrument, OrderSide.BUY, Quantity.of("5"), PRICE, TimeInForce.GTC, "resting-buy");
        book.process(restingBuy);

        Order incomingSell = Order.limit(owner, instrument, OrderSide.SELL, Quantity.of("5"), PRICE, TimeInForce.GTC, "incoming-sell");
        book.process(incomingSell);

        assertEquals(com.nextrade.common.enumtype.OrderStatus.CANCELLED, restingBuy.status());
        assertTrue(incomingSell.active(), "incoming sell should remain eligible to rest after the older own bid is removed");
        assertTrue(book.find(restingBuy.id()).isEmpty(), "cancelled resting order must be removed from the index");
        assertTrue(book.find(incomingSell.id()).isPresent(), "incoming sell must remain indexed as a resting ask");
    }

    private static Instrument instrument() {
        return new Instrument(InstrumentId.generate(), new com.nextrade.common.valueobject.Symbol("NEX"),
                "NexTrade Test", "NEX", "INR", 1, TICK, com.nextrade.common.enumtype.InstrumentStatus.ACTIVE);
    }

    private static User user(String username) {
        return User.register(username, username + "@example.test", "test-hash", UserRole.TRADER);
    }
}
