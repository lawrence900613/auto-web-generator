-- Chat history table (cursor-based pagination, soft delete)
CREATE TABLE IF NOT EXISTS chat_history
(
    id           BIGINT       NOT NULL,
    message      TEXT         NOT NULL,
    message_type VARCHAR(32)  NOT NULL,
    app_id       BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_delete    SMALLINT     DEFAULT 0                 NOT NULL,
    CONSTRAINT pk_chat_history PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_chat_history_app_id ON chat_history (app_id);
CREATE INDEX IF NOT EXISTS idx_chat_history_create_time ON chat_history (create_time);
CREATE INDEX IF NOT EXISTS idx_chat_history_app_id_create_time ON chat_history (app_id, create_time);
