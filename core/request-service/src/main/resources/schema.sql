CREATE TABLE IF NOT EXISTS requests
(
    id           BIGSERIAL PRIMARY KEY,
    created      TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    status       VARCHAR(20)                    NOT NULL,
    event_id     BIGINT,
    requester_id BIGINT                         NOT NULL,
    UNIQUE (event_id, requester_id)
);