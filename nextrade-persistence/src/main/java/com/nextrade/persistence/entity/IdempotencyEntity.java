package com.nextrade.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_entity")
public class IdempotencyEntity {
    @Id private UUID id;
    private String scope;
    private String keyValue;
    private int responseStatus;
    @Column(columnDefinition = "text") private String responseBody;
    private Instant expiresAt;
    @Version private long version;

    public IdempotencyEntity() {}
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public String getScope(){return scope;} public void setScope(String v){scope=v;}
    public String getKeyValue(){return keyValue;} public void setKeyValue(String v){keyValue=v;}
    public int getResponseStatus(){return responseStatus;} public void setResponseStatus(int v){responseStatus=v;}
    public String getResponseBody(){return responseBody;} public void setResponseBody(String v){responseBody=v;}
    public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant v){expiresAt=v;}
    public long getVersion(){return version;} public void setVersion(long v){version=v;}
}
