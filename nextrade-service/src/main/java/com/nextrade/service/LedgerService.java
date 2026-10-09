package com.nextrade.service;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.valueobject.Money;
import com.nextrade.persistence.entity.LedgerEntryEntity;
import com.nextrade.persistence.entity.LedgerJournalEntity;
import com.nextrade.persistence.repository.LedgerEntryRepository;
import com.nextrade.persistence.repository.LedgerJournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerService {

    private final LedgerEntryRepository ledgerRepository;
    private final LedgerJournalRepository journalRepository;

    public void recordTradeSettlement(com.nextrade.domain.order.Trade trade, Identifier.UserId buyerId, Identifier.UserId sellerId, Money tradeValue) {
        UUID journalId = UUID.randomUUID();
        Instant now = Instant.now();
        String currency = tradeValue.getCurrencyCode();

        // Buyer: cash debit, asset credit
        saveEntry(journalId, buyerId.getValue(), trade.getInstrument().getId(), "DEBIT", tradeValue.getAmount(), currency, "Trade settlement - cash out", "TRADE", trade.getId(), Instant.now());
        saveEntry(journalId, buyerId.getValue(), trade.getInstrument().getId(), "CREDIT", trade.getQuantity().getValue().setScale(4), "SHARES", "Trade settlement - shares in", "TRADE", trade.getId(), Instant.now());

        // Seller: asset debit, cash credit
        saveEntry(journalId, trade.getSellOrder().getUser().getId().getValue(), trade.getInstrument().getId(), "DEBIT", trade.getQuantity().getValue().setScale(4), "SHARES", "Trade settlement - shares out", "TRADE", trade.getId(), Instant.now());
        saveEntry(journalId, trade.getSellOrder().getUser().getId().getValue(), trade.getInstrument().getId(), "CREDIT", tradeValue.getAmount(), currency, "Trade settlement - cash in", "TRADE", trade.getId(), Instant.now());

        // Fees (if configured)
        // ledgerRepository.save(...)
    }

    private void saveEntry(UUID journalId, UUID accountId, Long instrumentId, String entryType, BigDecimal amount, String currency, String narrative, String refType, Long refId, Instant createdAt) {
        // Implementation
    }
}
