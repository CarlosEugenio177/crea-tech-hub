ALTER TABLE user_accounts DROP CONSTRAINT ck_user_accounts_access_role;

UPDATE user_accounts
SET access_role = CASE WHEN access_role = 'ADMIN' THEN 'ADMIN' ELSE 'USER' END;

ALTER TABLE user_accounts ADD CONSTRAINT ck_user_accounts_access_role
    CHECK (access_role IN ('ADMIN', 'USER'));

UPDATE user_accounts SET email = lower(trim(email));

ALTER TABLE user_accounts ADD CONSTRAINT ck_user_accounts_normalized_email
    CHECK (email = lower(trim(email)));

ALTER TABLE user_accounts ADD CONSTRAINT uk_user_accounts_email UNIQUE (email);
