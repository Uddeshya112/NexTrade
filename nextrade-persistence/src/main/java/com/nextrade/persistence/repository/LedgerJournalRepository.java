package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.LedgerJournalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LedgerJournalRepository extends JpaRepository<LedgerJournalEntity, UUID> {
    boolean existsByTradeId(UUID tradeId);
}
