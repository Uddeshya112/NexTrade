CREATE TABLE ledger_journal (
    id UUID PRIMARY KEY,
    trade_id UUID NOT NULL UNIQUE,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ledger_entry (
    id UUID PRIMARY KEY,
    journal_id UUID NOT NULL REFERENCES ledger_journal(id),
    account_id UUID NOT NULL,
    instrument_id UUID NOT NULL,
    entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount NUMERIC(24, 8) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(8) NOT NULL,
    narrative VARCHAR(200) NOT NULL,
    reference_type VARCHAR(30) NOT NULL,
    reference_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ledger_entry_journal ON ledger_entry(journal_id);
CREATE INDEX idx_ledger_entry_account ON ledger_entry(account_id, created_at);
