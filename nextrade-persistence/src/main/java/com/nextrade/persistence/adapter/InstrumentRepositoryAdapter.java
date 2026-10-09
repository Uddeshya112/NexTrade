package com.nextrade.persistence.adapter;

import com.nextrade.common.enumtype.InstrumentStatus;
import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Symbol;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.persistence.entity.InstrumentJpaEntity;
import com.nextrade.persistence.repository.InstrumentJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
@Transactional
public class InstrumentRepositoryAdapter implements InstrumentRepository {
    private final InstrumentJpaRepository jpaRepository;

    public InstrumentRepositoryAdapter(InstrumentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instrument> findById(InstrumentId id) {
        return jpaRepository.findByUuid(id.value()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Instrument> findBySymbol(Symbol symbol) {
        return jpaRepository.findBySymbol(symbol.value()).map(this::toDomain);
    }

    @Override
    public Instrument save(Instrument instrument) {
        InstrumentJpaEntity entity = jpaRepository.findByUuid(instrument.id().value()).orElseGet(InstrumentJpaEntity::new);
        entity.setUuid(instrument.id().value());
        entity.setSymbol(instrument.symbol().value());
        entity.setName(instrument.name());
        entity.setExchange(instrument.exchange());
        entity.setCurrency(instrument.currency());
        entity.setLotSize(instrument.lotSize());
        entity.setTickSize(instrument.tickSize().value());
        entity.setTickSizeTick(instrument.tickSize().tickSize());
        entity.setStatus(instrument.status().name());
        return toDomain(jpaRepository.save(entity));
    }

    private Instrument toDomain(InstrumentJpaEntity entity) {
        BigDecimal tick = entity.getTickSizeTick() == null ? entity.getTickSize() : entity.getTickSizeTick();
        return new Instrument(InstrumentId.of(entity.getUuid()), new Symbol(entity.getSymbol()), entity.getName(),
                entity.getExchange(), entity.getCurrency(), entity.getLotSize(),
                Price.of(entity.getTickSize(), tick), InstrumentStatus.valueOf(entity.getStatus()));
    }
}
