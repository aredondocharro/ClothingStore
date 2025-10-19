-- =========================================
-- 0) Limpiezas previas (evitar fallos de FK/UNIQUE)
-- =========================================

-- 0.1) Elimina sesiones huérfanas (user_id sin usuario)
DELETE FROM refresh_session rs
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = rs.user_id);

-- 0.2) Elimina tokens de reset huérfanos (por si los hubiera)
DELETE FROM password_reset_tokens t
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = t.user_id);

-- 0.3) Deduplicar roles (antes de imponer unicidad)
DELETE FROM user_roles ur
USING user_roles ur2
WHERE ur.user_id = ur2.user_id
  AND ur.role    = ur2.role
  AND ur.ctid    > ur2.ctid;  -- deja una sola fila por (user_id, role)


-- =========================================
-- 1) Coherencia de tipos temporales (UTC)
--    Si tus valores existentes están en UTC, conviértelos a TIMESTAMPTZ
-- =========================================

-- users.created_at: timestamp -> timestamptz
ALTER TABLE users
  ALTER COLUMN created_at TYPE timestamptz
  USING created_at AT TIME ZONE 'UTC';

-- refresh_session.*: timestamp -> timestamptz
ALTER TABLE refresh_session
  ALTER COLUMN expires_at TYPE timestamptz USING expires_at AT TIME ZONE 'UTC',
  ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
  ALTER COLUMN revoked_at TYPE timestamptz USING CASE
      WHEN revoked_at IS NULL THEN NULL
      ELSE revoked_at AT TIME ZONE 'UTC'
  END;


-- =========================================
-- 2) Índices redundantes
-- =========================================

-- UNIQUE(email) ya crea índice; elimina índice redundante si existía
DROP INDEX IF EXISTS idx_users_email;


-- =========================================
-- 3) Integridad y unicidad en user_roles
--    (usa CONSTRAINT, no sólo índice, para que ON CONFLICT funcione)
-- =========================================

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    WHERE c.conname = 'ux_user_roles_user_role'
      AND t.relname = 'user_roles'
  ) THEN
    ALTER TABLE user_roles
      ADD CONSTRAINT ux_user_roles_user_role
      UNIQUE (user_id, role);
  END IF;
END$$;


-- =========================================
-- 4) Foreign keys que faltan
-- =========================================

-- refresh_session.user_id → users.id  (ON DELETE CASCADE)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_name = 'fk_refresh_user'
      AND table_name = 'refresh_session'
  ) THEN
    ALTER TABLE refresh_session
      ADD CONSTRAINT fk_refresh_user
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
  END IF;
END$$;

-- password_reset_tokens.user_id → users.id  (ON DELETE CASCADE)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_name = 'fk_prt_user'
      AND table_name = 'password_reset_tokens'
  ) THEN
    ALTER TABLE password_reset_tokens
      ADD CONSTRAINT fk_prt_user
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
  END IF;
END$$;


-- =========================================
-- 5) Índices para consultas reales
-- =========================================

-- refresh_session: búsqueda por token_hash (verificación de refresh)
CREATE INDEX IF NOT EXISTS idx_refresh_token_hash
  ON refresh_session(token_hash);

-- password_reset_tokens: tokens activos (no usados) por usuario/orden caducidad
CREATE INDEX IF NOT EXISTS idx_prt_active
  ON password_reset_tokens (user_id, expires_at)
  WHERE used_at IS NULL;

-- email_outbox: cola PENDING priorizando por created_at
CREATE INDEX IF NOT EXISTS idx_outbox_pending
  ON email_outbox (created_at)
  WHERE status = 'PENDING';


-- =========================================
-- 6) Validación de estado en outbox
-- =========================================

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_name = 'chk_email_outbox_status'
      AND table_name = 'email_outbox'
  ) THEN
    ALTER TABLE email_outbox
      ADD CONSTRAINT chk_email_outbox_status
      CHECK (status IN ('PENDING','SENT','FAILED'));
  END IF;
END$$;


-- =========================================
-- 7) Seed ADMIN más robusto
--    (usa CONSTRAINT de (user_id, role) creada en paso 3)
-- =========================================

INSERT INTO users (id, email, password_hash, email_verified, created_at)
VALUES ('${adminId}'::uuid, '${adminEmail}', '${adminPasswordHash}', TRUE, NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT u.id, 'ADMIN'
FROM users u
WHERE u.email = '${adminEmail}'
ON CONFLICT ON CONSTRAINT ux_user_roles_user_role DO NOTHING;
