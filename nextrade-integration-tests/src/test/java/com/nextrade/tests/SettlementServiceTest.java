package com.nextrade.tests;

import com.nextrade.common.identifier.TradeId;
import com.nextrade.common.valueobject.Money;
import com.nextrade.domain.order.Trade;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.TradeRepository;
import com.nextrade.service.LedgerService;
import com.nextrade.service.SettlementService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

class SettlementServiceTest {
    @Test
    void persistsExecutionAndLedgerAsAnIdempotentSettlement() {
        var instrument = Fixture.instrument();
        Trade trade = new Trade(TradeId.generate(), com.nextrade.common.identifier.OrderId.generate(),
                com.nextrade.common.identifier.OrderId.generate(), Fixture.user("buyer").id(), Fixture.user("seller").id(),
                instrument.id(), Fixture.qty("2"), Fixture.price("12.50"), java.time.Instant.parse("2026-01-01T00:00:00Z"), 1);
        TradeRepository trades = mock(TradeRepository.class);
        InstrumentRepository instruments = mock(InstrumentRepository.class);
        LedgerService ledger = mock(LedgerService.class);
        when(trades.findById(trade.id())).thenReturn(Optional.empty(), Optional.of(trade));
        when(instruments.findById(instrument.id())).thenReturn(Optional.of(instrument));
        SettlementService service = new SettlementService(trades, instruments, ledger);

        service.settle(trade);
        service.settle(trade);

        verify(trades, times(1)).save(trade);
        verify(ledger, times(1)).recordTradeSettlement(trade, Money.of(new BigDecimal("25.0000"), "INR"));
    }

    @Test
    void doesNotCreateDuplicateLedgerEntriesWhenExecutionAlreadyExists() {
        var instrument = Fixture.instrument();
        Trade trade = new Trade(TradeId.generate(), com.nextrade.common.identifier.OrderId.generate(),
                com.nextrade.common.identifier.OrderId.generate(), Fixture.user("buyer2").id(), Fixture.user("seller2").id(),
                instrument.id(), Fixture.qty("1"), Fixture.price("5.00"), java.time.Instant.now(), 2);
        TradeRepository trades = mock(TradeRepository.class);
        InstrumentRepository instruments = mock(InstrumentRepository.class);
        LedgerService ledger = mock(LedgerService.class);
        when(trades.findById(trade.id())).thenReturn(Optional.of(trade));

        new SettlementService(trades, instruments, ledger).settle(trade);

        verifyNoInteractions(instruments, ledger);
        verify(trades, never()).save(any());
    }
}
