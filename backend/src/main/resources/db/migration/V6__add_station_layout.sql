ALTER TABLE spaces
    ADD COLUMN room_id UUID,
    ADD COLUMN station_number INTEGER,
    ADD COLUMN map_x NUMERIC(5, 2),
    ADD COLUMN map_y NUMERIC(5, 2),
    ADD COLUMN room_type VARCHAR(32) GENERATED ALWAYS AS (
        CASE WHEN room_id IS NOT NULL THEN 'ROOM' END
    ) STORED,
    ADD CONSTRAINT uq_spaces_id_type UNIQUE (id, type),
    ADD CONSTRAINT fk_spaces_station_room FOREIGN KEY (room_id, room_type) REFERENCES spaces (id, type),
    ADD CONSTRAINT uq_spaces_room_station_number UNIQUE (room_id, station_number),
    ADD CONSTRAINT ck_spaces_station_placement CHECK (
        (room_id IS NULL AND station_number IS NULL AND map_x IS NULL AND map_y IS NULL)
        OR (
            room_id IS NOT NULL AND station_number IS NOT NULL AND map_x IS NOT NULL AND map_y IS NOT NULL
            AND type = 'WORKSTATION' AND room_id <> id AND station_number > 0
            AND map_x BETWEEN 0 AND 100 AND map_y BETWEEN 0 AND 100
        )
    );
