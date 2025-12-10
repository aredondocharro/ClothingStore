-- V11__verification_tokens.sql
-- Tabla para almacenar los tokens de verificación de email
-- y permitir su rotación (revocar anteriores + registrar el nuevo)

create table if not exists verification_tokens (
    id         bigserial primary key,
    user_id    uuid        not null
        references users(id) on delete cascade,
    jti        uuid        not null unique,
    issued_at  timestamp   not null,
    expires_at timestamp   not null,
    revoked_at timestamp   null
);

create index if not exists idx_verification_tokens_user
    on verification_tokens(user_id);

create index if not exists idx_verification_tokens_revoked
    on verification_tokens(revoked_at);

create index if not exists idx_verification_tokens_expires
    on verification_tokens(expires_at);
