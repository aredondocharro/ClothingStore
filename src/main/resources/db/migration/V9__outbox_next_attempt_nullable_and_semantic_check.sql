-- 1) Permitir next_attempt_at nulo cuando el email ya no debe reintentar
ALTER TABLE email_outbox
  ALTER COLUMN next_attempt_at DROP NOT NULL;

-- 2) Rehacer el CHECK para que tenga sentido con los estados
ALTER TABLE email_outbox DROP CONSTRAINT IF EXISTS chk_email_outbox_status;

ALTER TABLE email_outbox
  ADD CONSTRAINT chk_email_outbox_status
  CHECK (
    (status IN ('PENDING','PROCESSING') AND next_attempt_at IS NOT NULL) OR
    (status IN ('SENT','FAILED')       AND next_attempt_at IS NULL)
  );
