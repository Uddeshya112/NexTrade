CREATE TABLE IF NOT EXISTS supabase_identity (
    supabase_user_id UUID PRIMARY KEY,
    nextrade_user_id UUID NOT NULL UNIQUE REFERENCES app_user(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
