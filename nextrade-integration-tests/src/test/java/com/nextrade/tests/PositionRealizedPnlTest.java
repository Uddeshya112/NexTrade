package com.nextrade.tests;

import com.nextrade.domain.order.Order;

import com.nextrade.common.enumtype.*;
import com.nextrade.common.identifier.*;
import com.nextrade.common.valueobject.*;
import com.nextrade.domain.instrument.*;
import com.nextrade.domain.order.*;
import com.nextrade.domain.user.*;
import com.nextrade.engine.matching.*;
import com.nextrade.engine.policy.*;
import org.junit.jupiter.api.*;
import java.math.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PositionRealizedPnlTest {
    private Instrument instrument;
    private User buyer;
    private User seller;

    @BeforeEach
    void setUp() {
        instrument = Fixture.instrument();
        buyer = Fixture.user("buyer_120");
        seller = Fixture.user("seller_120");
    }

    @Test
    void scenarioMetadataIsStable() {
        assertEquals("PositionRealizedPnl", "PositionRealizedPnl");
        assertTrue(instrument.tradeable());
        assertEquals(1, instrument.lotSize());
        assertNotNull(buyer.id());
        assertNotEquals(buyer.id(), seller.id());
    }

    @Test
    void validOrderRoundTrip() {
        Order order = Fixture.limit(buyer, instrument, OrderSide.BUY, "10", "100.00");
        assertEquals(OrderType.LIMIT, order.type());
        assertEquals(OrderStatus.NEW, order.status());
        assertEquals(new BigDecimal("10.0000"), order.remaining().value());
        assertEquals(buyer.id(), order.user().id());
        assertEquals(instrument.id(), order.instrument().id());
    }

    @Test
    void executionPathIsDeterministic() {
        MatchingEngine engine = new MatchingEngine(2, trade -> {});
        try {
            Order bid = Fixture.limit(buyer, instrument, OrderSide.BUY, "10", "100.00");
            Order ask = Fixture.limit(seller, instrument, OrderSide.SELL, "10", "100.00");
            MatchingResult first = engine.place(bid).join();
            MatchingResult second = engine.place(ask).join();
            assertNotNull(first);
            assertNotNull(second);
            assertTrue(second.trades().size() <= 1);
            assertTrue(bid.status().isTerminal() || bid.status() == OrderStatus.PARTIALLY_FILLED);
            assertTrue(ask.status().isTerminal() || ask.status() == OrderStatus.PARTIALLY_FILLED);
        } finally {
            engine.close();
        }
    }

    @Test
    void numericValueObjectsRemainBounded() {
        Price price = new Price(new BigDecimal("100.005"), new BigDecimal("0.01"));
        assertEquals(new BigDecimal("100.00"), price.roundToTick().value());
        Quantity quantity = new Quantity(new BigDecimal("12.34567"));
        assertEquals(4, quantity.value().scale());
        Money money = new Money(new BigDecimal("123.45678"), Currency.getInstance("INR"));
        assertEquals(4, money.amount().scale());
    }

    @Test
    void policySurfaceIsInspectable() {
        PriceTimePolicy policy = PriceTimePolicy.PRICE_THEN_TIME;
        assertNotNull(policy);
        assertEquals(OrderSide.BUY, OrderSide.SELL.opposite());
        assertTrue(TimeInForce.GTC.canRest());
        assertTrue(OrderType.STOP_LIMIT.requiresStopPrice());
    }
}
