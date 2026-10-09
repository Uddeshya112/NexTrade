package com.nextrade.persistence.adapter;

import com.nextrade.common.event.DomainEvent;
import com.nextrade.domain.repository.EventOutbox;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Temporary in-process adapter for local development. Replace with a transactional database outbox for deployment. */
@Repository
public final class OutboxRepositoryAdapter implements EventOutbox {
    private final List<DomainEvent> events = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void append(DomainEvent event) {
        events.add(java.util.Objects.requireNonNull(event, "event"));
    }

    public List<DomainEvent> snapshot() {
        synchronized (events) {
            return List.copyOf(events);
        }
    }
}
