CREATE TABLE space_blocks (
    id UUID PRIMARY KEY,
    space_id UUID NOT NULL REFERENCES spaces(id),
    starts_at TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    ends_at TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_by UUID NOT NULL REFERENCES user_accounts(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_by UUID REFERENCES user_accounts(id),
    cancelled_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_space_blocks_interval CHECK (ends_at > starts_at),
    CONSTRAINT ck_space_blocks_minutes CHECK (
        date_trunc('minute', starts_at) = starts_at AND date_trunc('minute', ends_at) = ends_at
    ),
    CONSTRAINT ck_space_blocks_reason CHECK (length(trim(reason)) > 0),
    CONSTRAINT ck_space_blocks_cancellation CHECK (
        (cancelled_at IS NULL AND cancelled_by IS NULL)
        OR (cancelled_at IS NOT NULL AND cancelled_by IS NOT NULL)
    )
);

CREATE INDEX ix_space_blocks_starts_id ON space_blocks (starts_at, id);
CREATE INDEX ix_space_blocks_space_interval ON space_blocks (space_id, starts_at, ends_at);
