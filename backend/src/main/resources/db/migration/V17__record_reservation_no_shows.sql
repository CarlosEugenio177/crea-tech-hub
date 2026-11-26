ALTER TABLE reservations
    ADD COLUMN no_show_at TIMESTAMPTZ,
    ADD COLUMN no_show_recorded_by UUID REFERENCES user_accounts (id),
    ADD CONSTRAINT ck_reservations_no_show CHECK (
        (no_show_at IS NULL AND no_show_recorded_by IS NULL)
        OR (status = 'CONFIRMED' AND no_show_at IS NOT NULL AND no_show_recorded_by IS NOT NULL)
    );
