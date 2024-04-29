
CREATE TABLE IF NOT EXISTS users
(
    user_id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(255)                            NOT NULL,
    email VARCHAR(512)                            NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (user_id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests
(
    request_id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description  VARCHAR(512)                            NOT NULL,
    requester_id BIGINT                                  NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_requests PRIMARY KEY (request_id),
    CONSTRAINT fk_requests_to_users FOREIGN KEY (requester_id) REFERENCES users (user_id)

);
CREATE TABLE IF NOT EXISTS items
(
    item_id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    owner_id     BIGINT                                  NOT NULL,
    name         VARCHAR(255)                            NOT NULL,
    description  VARCHAR(512)                            NOT NULL,
    is_available BOOLEAN,
    request_id   BIGINT,
    CONSTRAINT pk_items PRIMARY KEY (item_id),
    CONSTRAINT fk_items_to_users FOREIGN KEY (owner_id) REFERENCES users (user_id),
    CONSTRAINT fk_items_to_requests FOREIGN KEY (request_id) REFERENCES requests (request_id)
);
CREATE TABLE IF NOT EXISTS bookings
(
    booking_id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    item_id    BIGINT                                  NOT NULL,
    status     VARCHAR(8)                              NOT NULL,
    booker_id  BIGINT                                  NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_bookings PRIMARY KEY (booking_id),
    CONSTRAINT fk_bookings_to_users FOREIGN KEY (booker_id) REFERENCES users (user_id),
    CONSTRAINT fk_bookings_to_items FOREIGN KEY (item_id) REFERENCES items (item_id)
);
CREATE TABLE IF NOT EXISTS comments
(
    comment_id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    item_id   BIGINT                                  NOT NULL,
    text      VARCHAR(512)                            NOT NULL,
    author_id BIGINT                                  NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_comments PRIMARY KEY (comment_id),
    CONSTRAINT fk_comments_to_users FOREIGN KEY (author_id) REFERENCES users (user_id),
    CONSTRAINT fk_comments_to_items FOREIGN KEY (item_id) REFERENCES items (item_id)
);
