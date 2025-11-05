-- Adds retry/backoff fields and helpful indexes to email_outbox
ALTER TABLE email_outbox
    ADD COLUMN IF NOT EXISTS attempt_count     INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_attempt_at   TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS next_attempt_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS version           BIGINT       NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS sent_at           TIMESTAMPTZ;

-- status column already exists in V1 (PENDING/SENT). We'll also use PROCESSING/FAILED.

CREATE INDEX IF NOT EXISTS idx_email_outbox_due
    ON email_outbox (status, next_attempt_at);