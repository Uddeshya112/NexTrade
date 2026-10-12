package com.nextrade.service;

import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.identifier.TradeId;
import com.nextrade.common.valueobject.Money;
import com.nextrade.domain.order.Trade;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Persists each execution and its balanced audit journal in one database transaction. */
@Service
public class SettlementService {
    private final TradeRepository trades;
    private final InstrumentRepository instruments;
    private final LedgerService ledger;

    public SettlementService(TradeRepository trades, InstrumentRepository instruments, LedgerService ledger) {
        this.trades = trades;
        this.instruments = instruments;
        this.ledger = ledger;
    }

    @Transactional
    public void settle(Trade trade) {
        if (trades.findById(TradeId.of(trade.id().value())).isPresent()) return;
        var instrument = instruments.findById(InstrumentId.of(trade.instrumentId().value()))
                .orElseThrow(() -> new IllegalStateException("Trade references missing instrument " + trade.instrumentId()));
        trades.save(trade);
        BigDecimal notional = trade.price().value().multiply(trade.quantity().value());
        ledger.recordTradeSettlement(trade, Money.of(notional, instrument.currency()));
    }
}
