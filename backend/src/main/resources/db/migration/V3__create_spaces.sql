CREATE TABLE spaces (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(32) NOT NULL,
    capacity INTEGER NOT NULL,
    description VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_spaces_name CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_spaces_type CHECK (type IN ('ROOM', 'SHARED_TABLE', 'WORKSTATION')),
    CONSTRAINT ck_spaces_capacity CHECK (capacity > 0),
    CONSTRAINT ck_spaces_workstation_capacity CHECK (type <> 'WORKSTATION' OR capacity = 1)
);

CREATE INDEX ix_spaces_name_id ON spaces (name, id);
