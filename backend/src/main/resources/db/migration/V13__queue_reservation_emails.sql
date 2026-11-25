ALTER TABLE reservations
    ADD COLUMN email_tracking_hash VARCHAR(64) UNIQUE;

CREATE TABLE reservation_emails (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservation_id UUID NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    event_status VARCHAR(16) NOT NULL CHECK (event_status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED')),
    state VARCHAR(12) NOT NULL DEFAULT 'QUEUED' CHECK (state IN ('QUEUED', 'ACCEPTED', 'FAILED')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (reservation_id, event_status)
);

CREATE INDEX ix_reservation_emails_ready
    ON reservation_emails (available_at, id) WHERE state = 'QUEUED';

CREATE INDEX ix_reservation_emails_history
    ON reservation_emails (reservation_id, id DESC);
