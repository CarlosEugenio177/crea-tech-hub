CREATE INDEX ix_reservations_absences_by_cpf
    ON reservations (cpf) WHERE no_show_at IS NOT NULL;

CREATE TABLE requester_reservation_blocks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    decided_by UUID NOT NULL REFERENCES user_accounts (id),
    revoked_at TIMESTAMPTZ,
    revoked_by UUID REFERENCES user_accounts (id),
    CHECK (ends_at > starts_at),
    CHECK ((revoked_at IS NULL) = (revoked_by IS NULL))
);

CREATE INDEX ix_requester_reservation_blocks_active
    ON requester_reservation_blocks (cpf, ends_at DESC) WHERE revoked_at IS NULL;
