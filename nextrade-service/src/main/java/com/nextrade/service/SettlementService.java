package com.nextrade.service;

import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.identifier.TradeId;
import com.nextrade.common.valueobject.Money;
import com.nextrade.domain.order.Trade;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.domain.repository.TradeRepository;
import com.nextrade.engine.matching.TradeExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Persists each execution and its balanced audit journal in one database transaction. */
@Service
public class SettlementService {
    private final TradeRepository trades;
    private final InstrumentRepository instruments;
    private final OrderRepository orders;
    private final LedgerService ledger;

    @Autowired
    public SettlementService(TradeRepository trades, InstrumentRepository instruments, OrderRepository orders, LedgerService ledger) {
        this.trades = trades;
        this.instruments = instruments;
        this.orders = orders;
        this.ledger = ledger;
    }

    /** Compatibility constructor for callers that settle a trade without matcher order state. */
    public SettlementService(TradeRepository trades, InstrumentRepository instruments, LedgerService ledger) {
        this.trades = trades;
        this.instruments = instruments;
        this.orders = null;
        this.ledger = ledger;
    }

    @Transactional
    public void settle(TradeExecution execution) {
        settleTrade(execution.toTrade(), execution);
    }

    @Transactional
    public void settle(Trade trade) {
        settleTrade(trade, null);
    }

    private void settleTrade(Trade trade, TradeExecution execution) {
        if (trades.findById(TradeId.of(trade.id().value())).isPresent()) return;
        var instrument = instruments.findById(InstrumentId.of(trade.instrumentId().value()))
                .orElseThrow(() -> new IllegalStateException("Trade references missing instrument " + trade.instrumentId()));
        // Matching mutates both sides. Persist the resting counterparty too, in the same
        // transaction as the execution and ledger journal, so order history stays accurate.
        if (execution != null) {
            orders.save(execution.buyOrder());
            orders.save(execution.sellOrder());
        }
        trades.save(trade);
        BigDecimal notional = trade.price().value().multiply(trade.quantity().value());
        ledger.recordTradeSettlement(trade, Money.of(notional, instrument.currency()));
    }
}
