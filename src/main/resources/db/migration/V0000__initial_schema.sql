DROP SCHEMA IF EXISTS gw CASCADE;
CREATE SCHEMA gw;

-- keeps last known sequence that database is consistent to
CREATE TABLE gw.global_state (
    state_seq                 BIGINT NOT NULL
);

-- users
CREATE TABLE gw.users (
    uid                        BIGSERIAL,
    user_name                  VARCHAR(128) NOT NULL,
    user_email                 VARCHAR(128) NOT NULL,
    user_password_hash         VARCHAR(64) NOT NULL,
    user_state                 SMALLINT NOT NULL,
    user_state_seq             BIGINT NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (uid)
);

-- assets and currencies
CREATE TABLE gw.assets (
    asset_id                    SERIAL,
    asset_code                  VARCHAR(32) NOT NULL,
    asset_name                  VARCHAR(64) NOT NULL,
    asset_scale                 SMALLINT NOT NULL,
    asset_state                 SMALLINT NOT NULL,
    asset_state_seq             BIGINT NOT NULL,
    CONSTRAINT assets_pk PRIMARY KEY (asset_id)
);

-- symbols
CREATE TABLE gw.symbols (
    symbol_id                   SERIAL,
    symbol_code                 VARCHAR(32) NOT NULL,
    symbol_name                 VARCHAR(64) NOT NULL,
    symbol_type                 SMALLINT NOT NULL,
    symbol_base_asset_id        INTEGER NOT NULL REFERENCES gw.assets (asset_id),
    symbol_quote_asset_id       INTEGER NOT NULL REFERENCES gw.assets (asset_id),
    symbol_lot_size             NUMERIC(18,8) NOT NULL,
    symbol_step_size            NUMERIC(18,8) NOT NULL,
    symbol_taker_fee            NUMERIC(18,8) NOT NULL,
    symbol_margin_buy           NUMERIC(18,8) NOT NULL,
    symbol_price_high_limit     NUMERIC(18,8) NOT NULL,
    symbol_state                SMALLINT NOT NULL,
    symbol_state_seq            BIGINT NOT NULL,
    CONSTRAINT symbols_pk PRIMARY KEY (symbol_id)
);

-- all orders (pending, opened, closed)
CREATE TABLE gw.orders (
    order_id                    BIGSERIAL,
    order_uid                   BIGINT NOT NULL REFERENCES gw.users (uid),
    order_price_raw             BIGINT NOT NULL,
    order_size                  BIGINT NOT NULL,
    order_filled                BIGINT NOT NULL,
    order_user_cookie           INTEGER NOT NULL,
    order_action                SMALLINT NOT NULL,
    order_order_type            SMALLINT NOT NULL,
    order_create_time           TIMESTAMP WITH TIME ZONE NOT NULL,
    order_state                 SMALLINT NOT NULL,
    order_state_seq             BIGINT NOT NULL,
    CONSTRAINT orders_pk PRIMARY KEY (order_id)
);

-- all deals (trades)
CREATE TABLE gw.deals (
    deal_id                     BIGSERIAL,
    deal_taker_uid              BIGINT NOT NULL REFERENCES gw.users (uid),
    deal_taker_order_id         BIGINT NOT NULL REFERENCES gw.orders (order_id),
    deal_taker_action           SMALLINT NOT NULL,
    deal_maker_uid              BIGINT NOT NULL REFERENCES gw.users (uid),
    deal_maker_order_id         BIGINT NOT NULL REFERENCES gw.orders (order_id),
    deal_price_raw              BIGINT NOT NULL,
    deal_size                   BIGINT NOT NULL,
    deal_seq                    BIGINT NOT NULL,
    CONSTRAINT deals_pk PRIMARY KEY (deal_id)
);
