package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_entity")
public class OutboxEntity {
    @Id private UUID id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    @Column(columnDefinition = "text") private String payload;
    private String status;
    private Instant availableAt;
    private Instant claimedAt;
    private String claimedBy;
    private int attempts;
    @Version private long version;

    public OutboxEntity() {}
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public String getAggregateType(){return aggregateType;} public void setAggregateType(String v){aggregateType=v;}
    public String getAggregateId(){return aggregateId;} public void setAggregateId(String v){aggregateId=v;}
    public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
    public String getPayload(){return payload;} public void setPayload(String v){payload=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Instant getAvailableAt(){return availableAt;} public void setAvailableAt(Instant v){availableAt=v;}
    public Instant getClaimedAt(){return claimedAt;} public void setClaimedAt(Instant v){claimedAt=v;}
    public String getClaimedBy(){return claimedBy;} public void setClaimedBy(String v){claimedBy=v;}
    public int getAttempts(){return attempts;} public void setAttempts(int v){attempts=v;}
    public long getVersion(){return version;} public void setVersion(long v){version=v;}
}
