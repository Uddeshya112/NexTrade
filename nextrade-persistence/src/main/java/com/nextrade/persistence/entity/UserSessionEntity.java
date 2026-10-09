package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_session_entity")
public class UserSessionEntity {
    @Id private UUID id;
    private UUID userId;
    @Column(nullable = false, unique = true, length = 128) private String refreshDigest;
    private String status;
    private Instant expiresAt;
    private Instant createdAt;
    private UUID rotatedTo;
    @Version private long version;

    public UserSessionEntity() {}
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;}
    public String getRefreshDigest(){return refreshDigest;} public void setRefreshDigest(String v){refreshDigest=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public UUID getRotatedTo(){return rotatedTo;} public void setRotatedTo(UUID v){rotatedTo=v;}
    public long getVersion(){return version;} public void setVersion(long v){version=v;}
}
