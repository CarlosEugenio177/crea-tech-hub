CREATE TABLE reservation_decisions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservation_id UUID NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    decided_by UUID NOT NULL REFERENCES user_accounts(id),
    status VARCHAR(16) NOT NULL CHECK (status IN ('CONFIRMED', 'REJECTED', 'CANCELLED')),
    note VARCHAR(1000),
    decided_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_reservation_decisions_history
    ON reservation_decisions (reservation_id, id DESC);
