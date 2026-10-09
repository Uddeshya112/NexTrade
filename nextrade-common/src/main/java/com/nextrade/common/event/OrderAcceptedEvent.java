package com.nextrade.common.event;
import com.nextrade.common.identifier.EventId; import java.time.Instant;
public record OrderAcceptedEvent(EventId eventId, Instant occurredAt, String aggregateId, String summary) implements DomainEvent {
   public OrderAcceptedEvent{eventId=eventId==null?EventId.generate():eventId; occurredAt=occurredAt==null?Instant.now():occurredAt; if(aggregateId==null||aggregateId.isBlank())throw new IllegalArgumentException("aggregateId");}
   @Override public String aggregateType(){return "Order";}
}
