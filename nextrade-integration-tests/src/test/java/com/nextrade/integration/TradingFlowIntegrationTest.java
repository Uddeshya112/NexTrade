package com.nextrade.integration;

import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.user.User;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.engine.matching.MatchingResult;
import com.nextrade.tests.Fixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradingFlowIntegrationTest {
    @Test
    void limitOrdersMatchAndCompleteTheTradingFlow() {
        Instrument instrument = Fixture.instrument();
        User buyer = Fixture.user("integration_buyer");
        User seller = Fixture.user("integration_seller");

        try (MatchingEngine engine = new MatchingEngine(2, trade -> { })) {
            Order buy = Fixture.limit(buyer, instrument, OrderSide.BUY, "10", "100.00");
            Order sell = Fixture.limit(seller, instrument, OrderSide.SELL, "10", "100.00");

            MatchingResult buyResult = engine.place(buy).join();
            MatchingResult sellResult = engine.place(sell).join();

            assertTrue(buyResult.accepted());
            assertTrue(sellResult.accepted());
            assertEquals(com.nextrade.common.enumtype.OrderStatus.FILLED, buy.status());
            assertEquals(com.nextrade.common.enumtype.OrderStatus.FILLED, sell.status());
            assertEquals(1, sellResult.trades().size());
            assertEquals("10.0000", sellResult.trades().getFirst().quantity().value().toPlainString());
            assertEquals(instrument.id(), sellResult.trades().getFirst().instrumentId());
        }
    }
}
