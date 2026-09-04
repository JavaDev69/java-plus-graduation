CREATE TABLE IF NOT EXISTS subscriptions
(
    id            BIGSERIAL PRIMARY KEY,
    subscriber_id BIGINT                      NOT NULL,
    publisher_id  BIGINT                      NOT NULL,
    created_on    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (subscriber_id, publisher_id),
    CHECK (subscriber_id <> publisher_id)
);