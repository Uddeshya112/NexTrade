package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_entity")
public class EmailVerificationEntity {
    @Id private UUID id;
    private UUID userId;
    @Column(nullable = false, unique = true, length = 128) private String selector;
    @Column(nullable = false, length = 128) private String codeDigest;
    private Instant expiresAt;
    private int attempts;
    private int maxAttempts;
    private boolean used;
    private Instant usedAt;
    @Version private long version;

    public EmailVerificationEntity() {}
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;}
    public String getSelector(){return selector;} public void setSelector(String v){selector=v;}
    public String getCodeDigest(){return codeDigest;} public void setCodeDigest(String v){codeDigest=v;}
    public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;}
    public int getAttempts(){return attempts;} public void setAttempts(int v){attempts=v;}
    public int getMaxAttempts(){return maxAttempts;} public void setMaxAttempts(int v){maxAttempts=v;}
    public boolean getUsed(){return used;} public void setUsed(boolean v){used=v;}
    public Instant getUsedAt(){return usedAt;} public void setUsedAt(Instant v){usedAt=v;}
    public long getVersion(){return version;} public void setVersion(long v){version=v;}
}
