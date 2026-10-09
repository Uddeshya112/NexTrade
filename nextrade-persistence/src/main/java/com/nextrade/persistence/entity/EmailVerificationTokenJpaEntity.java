package com.nextrade.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_token",
    indexes = {
        @Index(name = "idx_verification_selector", columnList = "selector", unique = true),
        @Index(name = "idx_verification_code_hash", columnList = "code_hash", unique = true),
        @Index(name = "idx_verification_user", columnList = "user_id")
    })
@Data
@NoArgsConstructor
public class EmailVerificationTokenJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "selector", nullable = false, unique = true, length = 32)
    private String selector;

    @Column(name = "code_hash", nullable = false, unique = true, length = 64)
    private String codeHash;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 5;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public void markUsed() { this.used = true; this.usedAt = Instant.now(); }
    public void incrementAttempt() { this.attemptCount++; }

    @PrePersist void onCreate() { 
        if (id == null) id = UUID.randomUUID();
        if (selector == null) selector = generateSelector();
    }
    
    private String generateSelector() {
        return java.util.HexFormat.of().formatHex(new java.security.SecureRandom().generateSeed(16));
    }
}
