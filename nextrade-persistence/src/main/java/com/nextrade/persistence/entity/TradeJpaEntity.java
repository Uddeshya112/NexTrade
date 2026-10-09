package com.nextrade.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trade")
@Data
@NoArgsConstructor
public class TradeJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "buy_order_id", nullable = false)
    private Long buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private Long sellOrderId;

    @Column(name = "price", precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(name = "trade_time", nullable = false)
    private Instant tradeTime;

    @Column(name = "aggressor_side", length = 10, nullable = false)
    private String aggressorSide;

    @Column(name = "execution_id", nullable = false, unique = true)
    private UUID executionId;

    @PrePersist void onCreate() { if (uuid == null) uuid = UUID.randomUUID(); if (executionId == null) executionId = UUID.randomUUID(); tradeTime = Instant.now(); }
}
