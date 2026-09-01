CREATE TABLE IF NOT EXISTS ratings
(
    id       BIGSERIAL PRIMARY KEY,
    event_id BIGINT  NOT NULL,
    user_id  BIGINT  NOT NULL,
    is_like  BOOLEAN NOT NULL,
    UNIQUE (event_id, user_id)
);