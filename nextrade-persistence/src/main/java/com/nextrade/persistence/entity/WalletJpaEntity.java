package com.nextrade.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "wallet")
@Data
@NoArgsConstructor
public class WalletJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private java.util.UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private java.util.UUID userId;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "INR";

    @Column(name = "available_balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "reserved_balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal reservedBalance = BigDecimal.ZERO;

    @Version
    private Long version;

    @PrePersist
    void onCreate() { if (id == null) id = java.util.UUID.randomUUID(); }
}
