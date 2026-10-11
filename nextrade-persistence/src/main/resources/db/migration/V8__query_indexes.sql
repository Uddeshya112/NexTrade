-- Cover frequently joined foreign keys and support cleanup lookups.
CREATE INDEX IF NOT EXISTS idx_email_verification_user_id
    ON email_verification_entity(user_id);

CREATE INDEX IF NOT EXISTS idx_holding_instrument_id
    ON holding(instrument_id);

CREATE INDEX IF NOT EXISTS idx_password_reset_user_id
    ON password_reset_token(user_id);
