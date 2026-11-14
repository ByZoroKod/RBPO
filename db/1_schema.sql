CREATE TABLE users (
    id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255),
    username      VARCHAR(50),
    password_hash VARCHAR(100),
    role          VARCHAR(20) NOT NULL DEFAULT 'USER',
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT ck_users_name_not_blank CHECK (TRIM(name) <> ''),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'BLOCKED')),
    CONSTRAINT ck_users_credentials CHECK ((username IS NULL) = (password_hash IS NULL))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE collectible_items (
    id             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    description    VARCHAR(500) NOT NULL,
    owner_id       BIGINT NOT NULL,
    item_condition VARCHAR(100),
    CONSTRAINT ck_items_description_not_blank CHECK (TRIM(description) <> ''),
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE auction_lots (
    id             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id        BIGINT NOT NULL,
    start_price    DECIMAL(19, 2) NOT NULL,
    step           DECIMAL(19, 2) NOT NULL,
    start_time     DATETIME(6) NOT NULL,
    end_time       DATETIME(6) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    active_item_id BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN item_id END) VIRTUAL,
    CONSTRAINT fk_lots_item FOREIGN KEY (item_id) REFERENCES collectible_items (id) ON DELETE CASCADE,
    CONSTRAINT ck_lots_start_price CHECK (start_price > 0),
    CONSTRAINT ck_lots_step CHECK (step > 0),
    CONSTRAINT ck_lots_period CHECK (end_time > start_time),
    CONSTRAINT ck_lots_status CHECK (status IN ('DRAFT', 'ACTIVE', 'FINISHED', 'CANCELLED')),
    CONSTRAINT uq_lots_one_active_per_item UNIQUE (active_item_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE bids (
    id        BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lot_id    BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    amount    DECIMAL(19, 2) NOT NULL,
    placed_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_bids_lot FOREIGN KEY (lot_id) REFERENCES auction_lots (id) ON DELETE CASCADE,
    CONSTRAINT fk_bids_bidder FOREIGN KEY (bidder_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_bids_amount CHECK (amount > 0),
    CONSTRAINT uq_bids_id_lot_amount UNIQUE (id, lot_id, amount)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE auction_deals (
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lot_id          BIGINT NOT NULL,
    winning_bid_id  BIGINT NOT NULL,
    seller_id       BIGINT,
    amount          DECIMAL(19, 2) NOT NULL,
    transfer_status VARCHAR(20) NOT NULL,
    CONSTRAINT uq_deals_lot UNIQUE (lot_id),
    CONSTRAINT fk_deals_lot FOREIGN KEY (lot_id) REFERENCES auction_lots (id) ON DELETE CASCADE,
    CONSTRAINT fk_deals_seller FOREIGN KEY (seller_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_deals_winning_bid FOREIGN KEY (winning_bid_id, lot_id, amount)
        REFERENCES bids (id, lot_id, amount) ON DELETE CASCADE,
    CONSTRAINT ck_deals_amount CHECK (amount > 0),
    CONSTRAINT ck_deals_status CHECK (transfer_status IN ('PENDING', 'TRANSFERRED', 'CANCELLED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_sessions (
    id                   CHAR(36) NOT NULL PRIMARY KEY,
    username             VARCHAR(50) NOT NULL,
    device_id            VARCHAR(100),
    refresh_token        TEXT NOT NULL,
    refresh_token_hash   CHAR(64) GENERATED ALWAYS AS (SHA2(refresh_token, 256)) STORED,
    access_token_expiry  DATETIME(6) NOT NULL,
    refresh_token_expiry DATETIME(6) NOT NULL,
    status               VARCHAR(20) NOT NULL,
    CONSTRAINT uq_sessions_refresh_token UNIQUE (refresh_token_hash),
    CONSTRAINT fk_sessions_user FOREIGN KEY (username) REFERENCES users (username)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_sessions_status CHECK (status IN ('ACTIVE', 'USED', 'REVOKED')),
    CONSTRAINT ck_sessions_expiry CHECK (refresh_token_expiry > access_token_expiry)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
