package com.nextrade.integration;

import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.user.User;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.engine.matching.MatchingResult;
import com.nextrade.tests.Fixture;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingEngineConcurrencyTest {
    @Test
    void concurrentOrderPlacementIsSerializedPerInstrument() throws InterruptedException {
        Instrument instrument = Fixture.instrument();
        User buyer = Fixture.user("concurrency_buyer");
        User seller = Fixture.user("concurrency_seller");
        int orderCount = 200;
        AtomicInteger completed = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(orderCount);
        List<Thread> submitters = new ArrayList<>();

        try (MatchingEngine engine = new MatchingEngine(4, trade -> { })) {
            for (int i = 0; i < orderCount; i++) {
                final int index = i;
                Thread thread = new Thread(() -> {
                    try {
                        OrderSide side = (index % 2 == 0) ? OrderSide.BUY : OrderSide.SELL;
                        User owner = side == OrderSide.BUY ? buyer : seller;
                        Order order = Fixture.limit(owner, instrument, side, "1", "100.00");
                        MatchingResult result = engine.place(order).join();
                        if (result != null && result.accepted()) completed.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }, "nextrade-test-submitter-" + i);
                submitters.add(thread);
                thread.start();
            }
            assertTrue(latch.await(30, TimeUnit.SECONDS), "Timed out waiting for submitted orders");
            assertEquals(orderCount, completed.get());
        } finally {
            for (Thread thread : submitters) {
                thread.join(1000);
            }
        }
    }
}
