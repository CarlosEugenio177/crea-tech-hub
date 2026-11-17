ALTER TABLE spaces
    ADD COLUMN booking_mode VARCHAR(24) NOT NULL DEFAULT 'WHOLE_SPACE',
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_spaces_booking_mode CHECK (booking_mode IN ('WHOLE_SPACE', 'PER_STATION')),
    ADD CONSTRAINT ck_spaces_booking_mode_type CHECK (type = 'ROOM' OR booking_mode = 'WHOLE_SPACE');

UPDATE spaces room SET booking_mode = 'PER_STATION'
WHERE EXISTS (SELECT 1 FROM spaces station WHERE station.room_id = room.id);

ALTER TABLE spaces
    DROP CONSTRAINT fk_spaces_station_room,
    DROP CONSTRAINT uq_spaces_id_type,
    ADD COLUMN room_booking_mode VARCHAR(24) GENERATED ALWAYS AS (
        CASE WHEN room_id IS NOT NULL THEN 'PER_STATION' END
    ) STORED,
    ADD CONSTRAINT uq_spaces_id_type_mode UNIQUE (id, type, booking_mode),
    ADD CONSTRAINT fk_spaces_station_room FOREIGN KEY (room_id, room_type, room_booking_mode)
        REFERENCES spaces (id, type, booking_mode);
