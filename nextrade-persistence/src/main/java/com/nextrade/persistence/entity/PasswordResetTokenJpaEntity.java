package com.nextrade.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_token",
    indexes = { @Index(name = "idx_reset_token_hash", columnList = "token_hash", unique = true) })
@Data
@NoArgsConstructor
public class PasswordResetTokenJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public void markUsed() { this.used = true; this.usedAt = Instant.now(); }

    @PrePersist void onCreate() { if (id == null) id = UUID.randomUUID(); }
}
