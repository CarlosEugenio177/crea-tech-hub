CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    tracking_hash VARCHAR(64) NOT NULL UNIQUE,
    space_id UUID NOT NULL REFERENCES spaces(id),
    room_id UUID REFERENCES spaces(id),
    space_name VARCHAR(150) NOT NULL,
    station_number INTEGER,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    cpf VARCHAR(11) NOT NULL CHECK (cpf ~ '^[0-9]{11}$'),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    whatsapp VARCHAR(11) NOT NULL,
    profile VARCHAR(16) NOT NULL CHECK (profile IN ('EXTERNAL', 'MUTUALIST')),
    on_behalf_of_others BOOLEAN NOT NULL,
    reason VARCHAR(500) NOT NULL,
    notes VARCHAR(1000) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED')),
    rules_accepted_at TIMESTAMPTZ NOT NULL,
    CHECK (ends_at > starts_at),
    CHECK ((room_id IS NULL) = (station_number IS NULL))
);

CREATE INDEX ix_reservations_active_space
    ON reservations (space_id, starts_at, ends_at)
    WHERE status IN ('PENDING', 'CONFIRMED');

CREATE INDEX ix_reservations_active_room
    ON reservations (room_id, starts_at, ends_at)
    WHERE status IN ('PENDING', 'CONFIRMED') AND room_id IS NOT NULL;
