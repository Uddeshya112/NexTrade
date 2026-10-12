package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "supabase_identity", uniqueConstraints = @UniqueConstraint(name = "uq_supabase_identity_user", columnNames = "nextrade_user_id"))
public class SupabaseIdentityEntity {
    @Id
    @Column(name = "supabase_user_id", nullable = false, updatable = false)
    private UUID supabaseUserId;

    @Column(name = "nextrade_user_id", nullable = false, updatable = false)
    private UUID nextradeUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SupabaseIdentityEntity() { }

    public SupabaseIdentityEntity(UUID supabaseUserId, UUID nextradeUserId) {
        this.supabaseUserId = supabaseUserId;
        this.nextradeUserId = nextradeUserId;
        this.createdAt = Instant.now();
    }

    public UUID getSupabaseUserId() { return supabaseUserId; }
    public UUID getNextradeUserId() { return nextradeUserId; }
}
