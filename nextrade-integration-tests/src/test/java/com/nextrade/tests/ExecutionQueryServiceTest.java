package com.nextrade.tests;

import com.nextrade.common.identifier.OrderId;
import com.nextrade.common.identifier.TradeId;
import com.nextrade.domain.order.Trade;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.domain.repository.TradeRepository;
import com.nextrade.service.ExecutionQueryService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecutionQueryServiceTest {
    @Test
    void returnsPersistedExecutionsForOrderOwner() {
        var user = Fixture.user("orderowner");
        var order = Fixture.limit(user, Fixture.instrument(), com.nextrade.common.enumtype.OrderSide.BUY, "2", "10.00");
        Trade trade = new Trade(TradeId.generate(), order.id(), OrderId.generate(), user.id(), Fixture.user("otheruser").id(),
                order.instrument().id(), Fixture.qty("1"), Fixture.price("9.50"), Instant.parse("2026-01-01T00:00:00Z"), 3);
        OrderRepository orders = mock(OrderRepository.class);
        TradeRepository trades = mock(TradeRepository.class);
        when(orders.findById(order.id())).thenReturn(Optional.of(order));
        when(trades.findByOrder(order.id())).thenReturn(List.of(trade));

        var executions = new ExecutionQueryService(orders, trades).getExecutions(order.id().value(), user.id());

        assertEquals(1, executions.size());
        assertEquals("BUY", executions.getFirst().side());
        assertEquals("1", executions.getFirst().quantity());
        assertEquals("9.50", executions.getFirst().price());
    }

    @Test
    void rejectsCrossAccountExecutionReads() {
        var order = Fixture.limit(Fixture.user("owner1"), Fixture.instrument(), com.nextrade.common.enumtype.OrderSide.BUY, "1", "10");
        OrderRepository orders = mock(OrderRepository.class);
        TradeRepository trades = mock(TradeRepository.class);
        when(orders.findById(order.id())).thenReturn(Optional.of(order));

        assertThrows(SecurityException.class, () -> new ExecutionQueryService(orders, trades)
                .getExecutions(order.id().value(), Fixture.user("intruder1").id()));
        verifyNoInteractions(trades);
    }
}
