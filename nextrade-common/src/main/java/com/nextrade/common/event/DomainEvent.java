package com.nextrade.common.event;
import com.nextrade.common.identifier.EventId;
import java.time.Instant;
public interface DomainEvent { EventId eventId(); Instant occurredAt(); String aggregateType(); String aggregateId(); default String type(){return getClass().getSimpleName();} }
