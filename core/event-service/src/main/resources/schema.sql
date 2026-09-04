CREATE TABLE IF NOT EXISTS events
(
    id                 BIGSERIAL PRIMARY KEY,
    annotation         VARCHAR(2000)               NOT NULL,
    description        VARCHAR(7000),
    title              VARCHAR(120)                NOT NULL,
    paid               BOOLEAN                     NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    participant_limit  INTEGER                     NOT NULL DEFAULT 0,
    request_moderation BOOLEAN                     NOT NULL,
    location_lat       REAL,
    location_lon       REAL,
    confirmed_requests BIGINT                      NOT NULL DEFAULT 0,
    views              BIGINT                      NOT NULL DEFAULT 0,
    state              VARCHAR(20)                 NOT NULL,
    category_id        BIGINT,
    initiator_id       BIGINT                      NOT NULL
);

CREATE TABLE IF NOT EXISTS moderation_comments
(
    id           BIGSERIAL PRIMARY KEY,
    event_id     BIGINT        NOT NULL,
    comment_text VARCHAR(2000) NOT NULL,
    created_on   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
