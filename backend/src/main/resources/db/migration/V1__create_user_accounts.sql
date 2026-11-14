CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    access_role VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_accounts_full_name CHECK (length(trim(full_name)) > 0),
    CONSTRAINT ck_user_accounts_email CHECK (length(trim(email)) > 0),
    CONSTRAINT ck_user_accounts_password_hash CHECK (length(trim(password_hash)) > 0),
    CONSTRAINT ck_user_accounts_access_role CHECK (
        access_role IN ('REQUESTER', 'ADMIN', 'OPERATOR', 'BOARD', 'STAFF')
    )
);
