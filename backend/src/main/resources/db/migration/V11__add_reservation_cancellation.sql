ALTER TABLE reservations
    ADD COLUMN cancelled_at TIMESTAMPTZ,
    ADD COLUMN late_cancellation BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE reservations
SET cancelled_at = rules_accepted_at
WHERE status = 'CANCELLED' AND cancelled_at IS NULL;

ALTER TABLE reservations
    ADD CONSTRAINT ck_reservations_cancellation CHECK (
        (status = 'CANCELLED') = (cancelled_at IS NOT NULL)
    ),
    ADD CONSTRAINT ck_reservations_late_cancellation CHECK (
        NOT late_cancellation OR status = 'CANCELLED'
    );
