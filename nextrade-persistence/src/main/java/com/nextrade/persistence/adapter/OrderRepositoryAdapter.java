package com.nextrade.persistence.adapter;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.enumtype.OrderStatus;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.persistence.entity.OrderJpaEntity;
import com.nextrade.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Optional<Order> findById(Identifier.OrderId id) {
        return jpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<Order> findByClientOrderId(String clientOrderId) {
        return jpaRepository.findByClientOrderId(clientOrderId).map(this::toDomain);
    }

    @Override
    public List<Order> findByUserId(Identifier.UserId userId) {
        return jpaRepository.findByUserId(userId.getValue()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Order> findByUserIdAndStatus(Identifier.UserId userId, OrderStatus status) {
        return jpaRepository.findByUserIdAndStatus(userId.getValue(), status).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Order> findOpenOrdersByInstrument(Identifier.InstrumentId instrumentId) {
        return jpaRepository.findOpenOrdersByInstrument(instrumentId.getValue()).stream().map(this::toDomain).toList();
    }

    @Override
    public Order save(Order order) {
        return toDomain(jpaRepository.save(toEntity(order)));
    }

    @Override
    public long countByUserIdAndStatuses(Identifier.UserId userId, java.util.List<OrderStatus> statuses) {
        return jpaRepository.countByUserIdAndStatuses(userId.getValue(), statuses.stream().map(Enum::name).toList());
    }

    @Override
    public boolean existsByClientOrderId(String clientOrderId) {
        return jpaRepository.existsByClientOrderId(clientOrderId);
    }

    @Override
    public List<Order> findExpiredDayOrders(java.time.Instant now) {
        return jpaRepository.findExpiredDayOrders(now).stream().map(this::toDomain).toList();
    }

    @Override
    public Order findByUserIdWithLock(Identifier.UserId userId) {
        return jpaRepository.findByUserIdWithLock(userId.getValue()).map(this::toDomain).orElse(null);
    }

    private Order toDomain(OrderJpaEntity e) {
        return Order.reconstruct(
            Identifier.OrderId.of(e.getUuid()),
            Identifier.UserId.of(e.getUserId()),
            Identifier.InstrumentId.of(e.getInstrumentId()),
            e.getSide(),
            e.getType(),
            e.getQuantity(),
            e.getLimitPrice(),
            e.getStopPrice(),
            e.getTimeInForce(),
            e.getExpiresAt()
        );
    }

    private OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity e = new OrderJpaEntity();
        e.setUuid(order.getId().getValue());
        e.setClientOrderId(order.getClientOrderId());
        e.setUserId(order.getUser().getId().getValue());
        e.setInstrumentId(order.getInstrument().getId().getValue());
        e.setSide(order.getSide().name());
        e.setType(order.getType().name());
        e.setStatus(order.getStatus().name());
        e.setTimeInForce(order.getTimeInForce().name());
        e.setQuantity(order.getQuantity().getValue());
        e.setFilledQuantity(order.getFilledQuantity().getValue());
        e.setLimitPrice(order.getLimitPrice().map(Price::getValue).orElse(null));
        e.setLimitPriceTick(order.getLimitPrice().map(Price::getTickSize).orElse(null));
        e.setStopPrice(order.getStopPrice().map(Price::getValue).orElse(null));
        e.setStopPriceTick(order.getStopPrice().map(Price::getTickSize).orElse(null));
        e.setAvgFillPrice(order.getAvgFillPrice().map(Price::getValue).orElse(null));
        e.setAvgFillPriceTick(order.getAvgFillPrice().map(Price::getTickSize).orElse(null));
        e.setReservationPrice(order.getReservationPrice().map(Price::getValue).orElse(null));
        e.setReservationPriceTick(order.getReservationPrice().map(Price::getTickSize).orElse(null));
        e.setOriginalOrderId(order.getOriginalOrderId());
        e.setClientOrderId(order.getClientOrderId());
        e.setRevision(order.getRevision());
        e.setTriggeredAt(order.getTriggeredAt());
        e.setStopTriggerPrice(order.getStopTriggerPrice().map(Price::getValue).orElse(null));
        e.setExpiresAt(order.getExpiresAt());
        return e;
    }
}
