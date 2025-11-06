-- Añade clave de idempotencia al outbox y fuerza unicidad
ALTER TABLE email_outbox
  ADD COLUMN IF NOT EXISTS message_key VARCHAR(128);

-- Backfill seguro para filas existentes: clave estable "LEGACY-<id>"
UPDATE email_outbox
SET message_key = 'LEGACY-' || CAST(id AS VARCHAR)
WHERE message_key IS NULL;

-- Vuelve NOT NULL tras el backfill
ALTER TABLE email_outbox
  ALTER COLUMN message_key SET NOT NULL;

-- Índice único para evitar duplicados lógicos
CREATE UNIQUE INDEX IF NOT EXISTS ux_email_outbox_message_key
  ON email_outbox (message_key);
