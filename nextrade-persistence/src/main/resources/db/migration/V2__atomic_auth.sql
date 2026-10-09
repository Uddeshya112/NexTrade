create index if not exists ix_password_reset_active on password_reset_token(token_digest,expires_at) where used=false;
create index if not exists ix_email_verify_active on email_verification_entity(selector,expires_at) where used=false;
create index if not exists ix_sessions_user_status on user_session(user_id,status);
