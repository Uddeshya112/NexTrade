package com.nextrade.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "holding", uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "instrument_id"}))
@Data
@NoArgsConstructor
public class HoldingJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "available_quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "avg_buy_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal avgBuyPrice = BigDecimal.ZERO;

    @Column(name = "avg_buy_price_tick", precision = 19, scale = 4, nullable = false)
    private BigDecimal avgBuyPriceTick = BigDecimal.valueOf(0.05);

    @Column(name = "realized_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal realizedPnl = BigDecimal.ZERO;

    @Column(name = "realized_pnl_currency", length = 3, nullable = false)
    private String realizedPnlCurrency = "INR";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
}
