package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.InstrumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstrumentJpaRepository extends JpaRepository<InstrumentJpaEntity, Long> {
    Optional<InstrumentJpaEntity> findByUuid(UUID uuid);
    Optional<InstrumentJpaEntity> findBySymbol(String symbol);
}
