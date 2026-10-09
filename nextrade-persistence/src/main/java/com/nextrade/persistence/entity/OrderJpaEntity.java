package com.nextrade.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"order\"")
@Data
@NoArgsConstructor
public class OrderJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "client_order_id", length = 100)
    private String clientOrderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "side", nullable = false, length = 10)
    private String side;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "NEW";

    @Column(name = "time_in_force", length = 10, nullable = false)
    private String timeInForce = "DAY";

    @Column(name = "quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(name = "filled_quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    @Column(name = "limit_price", precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "limit_price_tick", precision = 19, scale = 4)
    private BigDecimal limitPriceTick;

    @Column(name = "stop_price", precision = 19, scale = 4)
    private BigDecimal stopPrice;

    @Column(name = "stop_price_tick", precision = 19, scale = 4)
    private BigDecimal stopPriceTick;

    @Column(name = "avg_fill_price", precision = 19, scale = 4)
    private BigDecimal avgFillPrice;

    @Column(name = "avg_fill_price_tick", precision = 19, scale = 4)
    private BigDecimal avgFillPriceTick;

    @Column(name = "reservation_price", precision = 19, scale = 4)
    private BigDecimal reservationPrice;

    @Column(name = "reservation_price_tick", precision = 19, scale = 4)
    private BigDecimal reservationPriceTick;

    @Column(name = "original_order_id")
    private UUID originalOrderId;


    @Column(name = "revision", nullable = false)
    private Long revision = 0L;

    @Column(name = "triggered_at")
    private java.time.Instant triggeredAt;

    @Column(name = "stop_trigger_price", precision = 19, scale = 4)
    private BigDecimal stopTriggerPrice;

    @Column(name = "stop_trigger_price_tick", precision = 19, scale = 4)
    private BigDecimal stopTriggerPriceTick;

    @Column(name = "expires_at")
    private java.time.Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist void onCreate() { if (uuid == null) uuid = java.util.UUID.randomUUID(); createdAt = Instant.now(); updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
