ALTER TABLE user_accounts ADD COLUMN security_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE user_accounts ADD COLUMN password_change_required BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE user_account_permissions (
    account_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    permission VARCHAR(64) NOT NULL,
    PRIMARY KEY (account_id, permission),
    CONSTRAINT ck_account_permission CHECK (permission IN ('MANAGE_ACCESS', 'MANAGE_SPACES'))
);

INSERT INTO user_account_permissions (account_id, permission)
SELECT id, permission FROM user_accounts
CROSS JOIN (VALUES ('MANAGE_ACCESS'), ('MANAGE_SPACES')) AS permissions(permission)
WHERE access_role = 'ADMIN';

CREATE TABLE admin_access_guard (
    id INTEGER PRIMARY KEY CHECK (id = 1)
);
INSERT INTO admin_access_guard (id) VALUES (1);

CREATE TABLE access_audit_events (
    id UUID PRIMARY KEY,
    actor_id UUID,
    target_id UUID NOT NULL,
    action VARCHAR(40) NOT NULL,
    detail VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_access_audit_created_id ON access_audit_events (created_at DESC, id DESC);

CREATE TABLE password_reset_tokens (
    account_id UUID PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    security_version BIGINT NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_reset_token_expiry CHECK (expires_at > requested_at)
);
