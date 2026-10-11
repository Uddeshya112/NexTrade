package com.nextrade.persistence.repository;

import com.nextrade.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByInstrumentIdAndStatusInOrderBySequenceAsc(UUID instrumentId, List<String> status);
    Optional<OrderEntity> findByUserIdAndClientOrderId(UUID userId, String clientOrderId);
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<OrderEntity> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);
}
