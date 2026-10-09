package com.nextrade.persistence.adapter;

import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.common.enumtype.OrderStatus;
import com.nextrade.common.enumtype.OrderType;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.identifier.OrderId;
import com.nextrade.common.identifier.UserId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.persistence.entity.OrderEntity;
import com.nextrade.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository jpaRepository;
    private final UserRepository users;
    private final InstrumentRepository instruments;

    public OrderRepositoryAdapter(OrderJpaRepository jpaRepository, UserRepository users, InstrumentRepository instruments) {
        this.jpaRepository = jpaRepository;
        this.users = users;
        this.instruments = instruments;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOpenByInstrument(InstrumentId id) {
        return jpaRepository.findByInstrumentIdAndStatusInOrderBySequenceAsc(
                id.value(), List.of(OrderStatus.NEW.name(), OrderStatus.PARTIALLY_FILLED.name()))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Order save(Order order) {
        return toDomain(jpaRepository.save(toEntity(order)));
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity entity = jpaRepository.findById(order.id().value()).orElseGet(OrderEntity::new);
        entity.setId(order.id().value());
        entity.setUserId(order.user().id().value());
        entity.setInstrumentId(order.instrument().id().value());
        entity.setSide(order.side().name());
        entity.setType(order.type().name());
        entity.setStatus(order.status().name());
        entity.setTif(order.tif().name());
        entity.setQuantity(order.workingQuantity().value());
        entity.setFilledQuantity(order.filledQuantity().value());
        entity.setLimitPrice(order.limitPrice() == null ? null : order.limitPrice().value());
        entity.setStopPrice(order.stopPrice() == null ? null : order.stopPrice().value());
        entity.setAvgFillPrice(order.avgFillPrice() == null ? null : order.avgFillPrice().value());
        entity.setClientOrderId(order.clientOrderId());
        entity.setExpiresAt(order.expiresAt());
        entity.setTriggeredAt(order.triggeredAt());
        entity.setSequence(order.sequence());
        entity.setCreatedAt(order.createdAt());
        entity.setUpdatedAt(order.updatedAt());
        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        User user = users.findById(UserId.of(entity.getUserId()))
                .orElseThrow(() -> new IllegalStateException("Order references missing user " + entity.getUserId()));
        Instrument instrument = instruments.findById(InstrumentId.of(entity.getInstrumentId()))
                .orElseThrow(() -> new IllegalStateException("Order references missing instrument " + entity.getInstrumentId()));
        BigDecimal tick = instrument.tickSize().tickSize();
        Price limit = entity.getLimitPrice() == null ? null : Price.of(entity.getLimitPrice(), tick);
        Price stop = entity.getStopPrice() == null ? null : Price.of(entity.getStopPrice(), tick);
        Price average = entity.getAvgFillPrice() == null ? null : Price.of(entity.getAvgFillPrice(), tick);
        Instant created = entity.getCreatedAt() == null ? Instant.now() : entity.getCreatedAt();
        Instant updated = entity.getUpdatedAt() == null ? created : entity.getUpdatedAt();
        return Order.reconstitute(OrderId.of(entity.getId()), user, instrument,
                OrderSide.valueOf(entity.getSide()), OrderType.valueOf(entity.getType()),
                new Quantity(entity.getQuantity()), new Quantity(entity.getQuantity()),
                new Quantity(entity.getFilledQuantity()), limit, stop, average,
                TimeInForce.valueOf(entity.getTif()), entity.getClientOrderId(),
                OrderStatus.valueOf(entity.getStatus()), entity.getExpiresAt(), entity.getTriggeredAt(),
                entity.getSequence(), created, updated, entity.getVersion());
    }
}
