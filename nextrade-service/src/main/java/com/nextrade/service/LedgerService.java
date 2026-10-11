package com.nextrade.service;

import com.nextrade.common.valueobject.Money;
import com.nextrade.domain.order.Trade;
import com.nextrade.persistence.entity.LedgerEntryEntity;
import com.nextrade.persistence.entity.LedgerJournalEntity;
import com.nextrade.persistence.repository.LedgerEntryRepository;
import com.nextrade.persistence.repository.LedgerJournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerEntryRepository entryRepository;
    private final LedgerJournalRepository journalRepository;

    @Transactional
    public void recordTradeSettlement(Trade trade, Money tradeValue) {
        if (journalRepository.existsByTradeId(trade.id().value())) return;
        UUID journalId = UUID.randomUUID();
        Instant now = Instant.now();
        String cashCurrency = tradeValue.currency().getCurrencyCode();
        UUID instrumentId = trade.instrumentId().value();
        UUID tradeId = trade.id().value();

        journalRepository.save(new LedgerJournalEntity(journalId, tradeId, cashCurrency, now));
        entryRepository.saveAll(List.of(
                entry(journalId, trade.buyer().value(), instrumentId, "DEBIT", tradeValue.amount(), cashCurrency, "Trade cash paid", tradeId, now),
                entry(journalId, trade.buyer().value(), instrumentId, "CREDIT", trade.quantity().value(), "SHARES", "Trade shares received", tradeId, now),
                entry(journalId, trade.seller().value(), instrumentId, "DEBIT", trade.quantity().value(), "SHARES", "Trade shares delivered", tradeId, now),
                entry(journalId, trade.seller().value(), instrumentId, "CREDIT", tradeValue.amount(), cashCurrency, "Trade cash received", tradeId, now)
        ));
    }

    private LedgerEntryEntity entry(UUID journalId, UUID accountId, UUID instrumentId, String type,
                                    java.math.BigDecimal amount, String currency, String narrative,
                                    UUID tradeId, Instant createdAt) {
        return new LedgerEntryEntity(UUID.randomUUID(), journalId, accountId, instrumentId,
                type, amount, currency, narrative, "TRADE", tradeId, createdAt);
    }
}
