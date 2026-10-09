package com.nextrade.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "instrument")
@Data
@NoArgsConstructor
public class InstrumentJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private java.util.UUID uuid;

    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(name = "isin", length = 12, unique = true)
    private String isin;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "exchange", length = 20, nullable = false)
    private String exchange = "NSE";

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "INR";

    @Column(name = "lot_size", nullable = false)
    private Integer lotSize = 1;

    @Column(name = "tick_size", precision = 19, scale = 4, nullable = false)
    private BigDecimal tickSize = BigDecimal.valueOf(0.05);

    @Column(name = "tick_size_tick", precision = 19, scale = 4)
    private BigDecimal tickSizeTick = BigDecimal.valueOf(0.05);

    @Column(name = "status", length = 20, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "meta_json", columnDefinition = "JSONB")
    private String metaJson = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist void onCreate() { if (uuid == null) uuid = java.util.UUID.randomUUID(); createdAt = Instant.now(); updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
