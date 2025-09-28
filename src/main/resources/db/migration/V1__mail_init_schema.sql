CREATE TABLE IF NOT EXISTS email_outbox (
    id               BIGSERIAL PRIMARY KEY,
    to_addresses     TEXT NOT NULL,
    subject          TEXT NOT NULL,
    body             TEXT NOT NULL,
    is_html          BOOLEAN NOT NULL DEFAULT TRUE,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_error       TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at          TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_email_outbox_status ON email_outbox(status);
