package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_journal")
@NoArgsConstructor
public class LedgerJournalEntity {
    @Id @Column(name = "id", nullable = false) private UUID id;
    @Column(name = "trade_id", nullable = false, unique = true) private UUID tradeId;
    @Column(nullable = false, length = 3) private String currency;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    public LedgerJournalEntity(UUID id, UUID tradeId, String currency, Instant createdAt) {
        this.id = id; this.tradeId = tradeId; this.currency = currency; this.createdAt = createdAt;
    }
}
