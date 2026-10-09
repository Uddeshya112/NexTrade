package com.nextrade.domain.repository;

import com.nextrade.common.event.DomainEvent;

import java.util.Collection;

/** Application-facing event outbox port; persistence implementations depend inward on this contract. */
public interface EventOutbox {
    void append(DomainEvent event);
    default void appendAll(Collection<? extends DomainEvent> events) {
        for (DomainEvent event : events) append(event);
    }
}
