ALTER TABLE spaces
    ADD COLUMN min_booking_minutes INTEGER,
    ADD COLUMN max_booking_minutes INTEGER,
    ADD CONSTRAINT ck_spaces_booking_duration CHECK (
        (min_booking_minutes IS NULL OR min_booking_minutes BETWEEN 1 AND 1439)
        AND (max_booking_minutes IS NULL OR max_booking_minutes BETWEEN 1 AND 1439)
        AND (min_booking_minutes IS NULL OR max_booking_minutes IS NULL
            OR min_booking_minutes <= max_booking_minutes)
    );
