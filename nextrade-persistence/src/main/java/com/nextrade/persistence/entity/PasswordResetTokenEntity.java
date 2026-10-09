package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_token_entity")
public class PasswordResetTokenEntity {
    @Id private UUID id;
    private UUID userId;
    @Column(length = 128, nullable = false, unique = true) private String tokenDigest;
    private Instant expiresAt;
    private boolean used;
    private Instant usedAt;
    @Version private long version;

    public PasswordResetTokenEntity() {}
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;}
    public String getTokenDigest(){return tokenDigest;} public void setTokenDigest(String v){tokenDigest=v;}
    public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;}
    public boolean getUsed(){return used;} public void setUsed(boolean v){used=v;}
    public Instant getUsedAt(){return usedAt;} public void setUsedAt(Instant v){usedAt=v;}
    public long getVersion(){return version;} public void setVersion(long v){version=v;}
}
