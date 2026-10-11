CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50),
    aggregate_id UUID,
    event_version INTEGER NOT NULL DEFAULT 1,
    schema_version INTEGER NOT NULL DEFAULT 1,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    failure_reason VARCHAR(1000),
    correlation_id UUID,
    causation_id UUID,
    command_id UUID,
    partition_key VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_outbox_event_unpublished
    ON outbox_event(published, created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_aggregate
    ON outbox_event(aggregate_type, aggregate_id);
