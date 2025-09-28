-- Usuarios
create table if not exists users(
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    email_verified boolean not null default false,
    created_at timestamp not null
);

-- Roles
create table if not exists user_roles(
    user_id uuid not null references users(id) on delete cascade,
    role varchar(50) not null
);

create index if not exists idx_users_email on users(email);
