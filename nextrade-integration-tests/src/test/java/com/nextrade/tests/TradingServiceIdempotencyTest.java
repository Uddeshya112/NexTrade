package com.nextrade.tests;

import com.nextrade.common.exception.IdempotencyConflictException;
import com.nextrade.common.identifier.*;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.contracts.trading.OrderRequest;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.service.MarketDataService;
import com.nextrade.service.TradingService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletionException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradingServiceIdempotencyTest {
    @Test
    void retryReturnsOriginalOrderAndDoesNotMatchItTwice() {
        var user = Fixture.user("retry_buyer");
        user.verifyEmail();
        var instrument = Fixture.instrument();
        UserId userId = user.id();
        InMemoryOrders orders = new InMemoryOrders();
        InstrumentRepository instruments = mock(InstrumentRepository.class);
        UserRepository users = mock(UserRepository.class);
        MarketDataService market = mock(MarketDataService.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(instruments.findById(instrument.id())).thenReturn(Optional.of(instrument));
        MatchingEngine matching = new MatchingEngine(1, trade -> {});
        try {
            TradingService service = new TradingService(orders, instruments, users, matching, market);
            OrderRequest request = new OrderRequest("request-1", instrument.id().asString(), "BUY", "LIMIT", "10", "100.00", null, "GTC");
            var first = service.placeOrder(request, userId).join();
            var retry = service.placeOrder(request, userId).join();
            assertEquals(first.orderId(), retry.orderId());
            assertEquals(2, orders.saves);
            assertEquals(1, matching.processed());
        } finally {
            matching.close();
        }
    }

    @Test
    void changedPayloadWithSameClientOrderIdIsRejected() {
        var user = Fixture.user("retry_conflict");
        user.verifyEmail();
        var instrument = Fixture.instrument();
        UserId userId = user.id();
        InMemoryOrders orders = new InMemoryOrders();
        orders.save(Order.limit(user, instrument, com.nextrade.common.enumtype.OrderSide.BUY,
                Quantity.of("10"), new Price(new BigDecimal("100.00"), new BigDecimal("0.01")),
                com.nextrade.common.enumtype.TimeInForce.GTC, "request-2"));
        InstrumentRepository instruments = mock(InstrumentRepository.class);
        UserRepository users = mock(UserRepository.class);
        MarketDataService market = mock(MarketDataService.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        MatchingEngine matching = new MatchingEngine(1, trade -> {});
        try {
            TradingService service = new TradingService(orders, instruments, users, matching, market);
            OrderRequest changed = new OrderRequest("request-2", instrument.id().asString(), "BUY", "LIMIT", "11", "100.00", null, "GTC");
            CompletionException error = assertThrows(CompletionException.class, () -> service.placeOrder(changed, userId).join());
            assertInstanceOf(IdempotencyConflictException.class, error.getCause());
            assertEquals(1, orders.saves);
            assertEquals(0, matching.processed());
        } finally {
            matching.close();
        }
    }

    @Test
    void concurrentUniqueKeyWinnerIsReturnedWithoutMatchingAgain() {
        var user = Fixture.user("retry_race");
        user.verifyEmail();
        var instrument = Fixture.instrument();
        UserId userId = user.id();
        InMemoryOrders orders = new InMemoryOrders();
        Order existing = Order.limit(user, instrument, com.nextrade.common.enumtype.OrderSide.BUY,
                Quantity.of("10"), new Price(new BigDecimal("100.00"), new BigDecimal("0.01")),
                com.nextrade.common.enumtype.TimeInForce.GTC, "request-race");
        orders.seed(existing);
        orders.hideExistingOnce = true;
        orders.failNextSaveAsDuplicate = true;
        InstrumentRepository instruments = mock(InstrumentRepository.class);
        UserRepository users = mock(UserRepository.class);
        MarketDataService market = mock(MarketDataService.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(instruments.findById(instrument.id())).thenReturn(Optional.of(instrument));
        MatchingEngine matching = new MatchingEngine(1, trade -> {});
        try {
            TradingService service = new TradingService(orders, instruments, users, matching, market);
            OrderRequest request = new OrderRequest("request-race", instrument.id().asString(), "BUY", "LIMIT", "10", "100.00", null, "GTC");
            var result = service.placeOrder(request, userId).join();
            assertEquals(existing.id().asString(), result.orderId());
            assertEquals(1, orders.saveAttempts);
            assertEquals(0, matching.processed());
        } finally {
            matching.close();
        }
    }

    private static final class InMemoryOrders implements OrderRepository {
        private final Map<String, Order> values = new HashMap<>();
        private int saves;
        private int saveAttempts;
        private boolean hideExistingOnce;
        private boolean failNextSaveAsDuplicate;
        void seed(Order order) { values.put(order.user().id().asString() + ":" + order.clientOrderId(), order); }
        @Override public Optional<Order> findById(OrderId id) { return values.values().stream().filter(o -> o.id().equals(id)).findFirst(); }
        @Override public List<Order> findOpenByInstrument(InstrumentId id) { return List.of(); }
        @Override public Optional<Order> findByUserIdAndClientOrderId(UserId userId, String clientOrderId) {
            if (hideExistingOnce) { hideExistingOnce = false; return Optional.empty(); }
            return Optional.ofNullable(values.get(userId.asString() + ":" + clientOrderId));
        }
        @Override public Order save(Order order) {
            saveAttempts++;
            if (failNextSaveAsDuplicate) { failNextSaveAsDuplicate = false; throw new DataIntegrityViolationException("simulated concurrent unique-key collision"); }
            values.put(order.user().id().asString() + ":" + order.clientOrderId(), order);
            saves++;
            return order;
        }
    }
}
