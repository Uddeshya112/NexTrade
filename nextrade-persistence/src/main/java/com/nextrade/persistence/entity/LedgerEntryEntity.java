package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
@NoArgsConstructor
public class LedgerEntryEntity {
    @Id @Column(name = "id", nullable = false) private UUID id;
    @Column(name = "journal_id", nullable = false) private UUID journalId;
    @Column(name = "account_id", nullable = false) private UUID accountId;
    @Column(name = "instrument_id", nullable = false) private UUID instrumentId;
    @Column(name = "entry_type", nullable = false, length = 10) private String entryType;
    @Column(nullable = false, precision = 24, scale = 8) private BigDecimal amount;
    @Column(nullable = false, length = 8) private String currency;
    @Column(nullable = false, length = 200) private String narrative;
    @Column(name = "reference_type", nullable = false, length = 30) private String referenceType;
    @Column(name = "reference_id", nullable = false) private UUID referenceId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    public LedgerEntryEntity(UUID id, UUID journalId, UUID accountId, UUID instrumentId, String entryType,
                             BigDecimal amount, String currency, String narrative, String referenceType,
                             UUID referenceId, Instant createdAt) {
        this.id = id; this.journalId = journalId; this.accountId = accountId; this.instrumentId = instrumentId;
        this.entryType = entryType; this.amount = amount; this.currency = currency; this.narrative = narrative;
        this.referenceType = referenceType; this.referenceId = referenceId; this.createdAt = createdAt;
    }
}
