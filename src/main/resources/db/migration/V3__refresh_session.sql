create table if not exists refresh_session (
  jti              varchar(36) primary key,
  user_id          uuid not null,
  expires_at       timestamp not null,
  created_at       timestamp not null,
  revoked_at       timestamp null,
  replaced_by_jti  varchar(36) null,
  token_hash       varchar(64) not null,
  ip               varchar(64) null,
  user_agent       varchar(256) null
);

create index if not exists idx_refresh_user on refresh_session(user_id);
create index if not exists idx_refresh_revoked on refresh_session(revoked_at);
create index if not exists idx_refresh_expires on refresh_session(expires_at);