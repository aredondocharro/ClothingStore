create table if not exists password_reset_tokens (
    id uuid primary key,
    user_id uuid not null,
    token_hash varchar(128) not null,
    expires_at timestamp with time zone not null,
    used_at timestamp with time zone null,
    created_at timestamp with time zone not null
);

create index if not exists idx_prt_token_hash on password_reset_tokens (token_hash);
create index if not exists idx_prt_user_expires on password_reset_tokens (user_id, expires_at);
