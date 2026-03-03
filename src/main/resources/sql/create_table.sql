-- PostgreSQL DDL for the user module.
-- Run against your target database (auto_web_generator).
--
-- Step 1 (run once as superuser): CREATE DATABASE auto_web_generator;
-- Step 2: Run this script against that database.

-- 'user' is a reserved word in PostgreSQL, so we use 'users'.
CREATE TABLE IF NOT EXISTS users
(
    id            bigint       NOT NULL,                   -- Snowflake ID set by the application
    user_account  varchar(256) NOT NULL,
    user_password varchar(512) NOT NULL,
    user_name     varchar(256),
    user_avatar   varchar(1024),
    user_profile  varchar(512),
    user_role     varchar(256) NOT NULL DEFAULT 'user',    -- 'user' | 'admin'
    edit_time     timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete     smallint     NOT NULL DEFAULT 0,          -- 0 = active, 1 = deleted
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_user_account ON users (user_account);

-- PostgreSQL has no ON UPDATE CURRENT_TIMESTAMP; use a trigger instead.
CREATE OR REPLACE FUNCTION fn_set_update_time()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_update_time ON users;
CREATE TRIGGER trg_users_update_time
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION fn_set_update_time();

-- App table
CREATE TABLE IF NOT EXISTS app
(
    id            bigint        NOT NULL,                   -- Snowflake ID set by the application
    app_name      varchar(256),
    cover         varchar(512),
    init_prompt   text,
    code_gen_type varchar(64),                              -- 'html' | 'multi_file'
    deploy_key    varchar(64)   UNIQUE,                     -- 6-char alphanumeric deploy identifier
    deployed_time timestamp,
    priority      int           NOT NULL DEFAULT 0,         -- 99 = featured
    user_id       bigint        NOT NULL,
    edit_time     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete     smallint      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_app_name ON app (app_name);
CREATE INDEX IF NOT EXISTS idx_app_user_id ON app (user_id);

DROP TRIGGER IF EXISTS trg_app_update_time ON app;
CREATE TRIGGER trg_app_update_time
    BEFORE UPDATE ON app
    FOR EACH ROW EXECUTE FUNCTION fn_set_update_time();
