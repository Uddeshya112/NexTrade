package com.nextrade.persistence.adapter;

import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.identifier.OrderId;
import com.nextrade.common.identifier.TradeId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.order.Trade;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.TradeRepository;
import com.nextrade.common.identifier.UserId;
import com.nextrade.persistence.entity.TradeEntity;
import com.nextrade.persistence.repository.TradeJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TradeRepositoryAdapter implements TradeRepository {
    private final TradeJpaRepository trades;
    private final InstrumentRepository instruments;

    public TradeRepositoryAdapter(TradeJpaRepository trades, InstrumentRepository instruments) {
        this.trades = trades;
        this.instruments = instruments;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trade> findById(TradeId id) {
        return trades.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Trade save(Trade trade) {
        TradeEntity entity = trades.findById(trade.id().value()).orElseGet(TradeEntity::new);
        entity.setId(trade.id().value());
        entity.setBuyOrderId(trade.buyOrderId().value());
        entity.setSellOrderId(trade.sellOrderId().value());
        entity.setBuyerId(trade.buyer().value());
        entity.setSellerId(trade.seller().value());
        entity.setInstrumentId(trade.instrumentId().value());
        entity.setQuantity(trade.quantity().value());
        entity.setPrice(trade.price().value());
        entity.setSequence(trade.sequence());
        entity.setExecutedAt(trade.executedAt());
        trades.save(entity);
        return trade;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trade> findByOrder(OrderId id) {
        return trades.findByBuyOrderIdOrSellOrderId(id.value(), id.value()).stream()
                .sorted(Comparator.comparingLong(TradeEntity::getSequence)).map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trade> findByInstrument(InstrumentId id) {
        return trades.findByInstrumentIdOrderBySequenceAsc(id.value()).stream().map(this::toDomain).toList();
    }

    private Trade toDomain(TradeEntity entity) {
        Instrument instrument = instruments.findById(InstrumentId.of(entity.getInstrumentId()))
                .orElseThrow(() -> new IllegalStateException("Trade references missing instrument " + entity.getInstrumentId()));
        return new Trade(TradeId.of(entity.getId()), OrderId.of(entity.getBuyOrderId()),
                OrderId.of(entity.getSellOrderId()), UserId.of(entity.getBuyerId()), UserId.of(entity.getSellerId()),
                InstrumentId.of(entity.getInstrumentId()), new Quantity(entity.getQuantity()),
                Price.of(entity.getPrice(), instrument.tickSize().tickSize()), entity.getExecutedAt(), entity.getSequence());
    }
}
