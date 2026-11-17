ALTER TABLE spaces
    ADD COLUMN room_width NUMERIC(5, 2),
    ADD COLUMN room_length NUMERIC(5, 2);

UPDATE spaces SET room_width = 10, room_length = 6 WHERE type = 'ROOM';

ALTER TABLE spaces ADD CONSTRAINT ck_spaces_room_dimensions CHECK (
    (type = 'ROOM' AND room_width IS NOT NULL AND room_length IS NOT NULL
        AND room_width BETWEEN 1 AND 100 AND room_length BETWEEN 1 AND 100)
    OR (room_width IS NULL AND room_length IS NULL)
);
