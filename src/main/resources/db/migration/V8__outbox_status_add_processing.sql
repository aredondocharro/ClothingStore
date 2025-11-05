ALTER TABLE email_outbox DROP CONSTRAINT IF EXISTS chk_email_outbox_status;

ALTER TABLE email_outbox
  ADD CONSTRAINT chk_email_outbox_status
  CHECK (status IN ('PENDING','PROCESSING','SENT','FAILED'));
  CREATE INDEX IF NOT EXISTS idx_email_outbox_due
    ON email_outbox (status, next_attempt_at);