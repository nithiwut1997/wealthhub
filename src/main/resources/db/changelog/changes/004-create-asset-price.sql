-- liquibase formatted sql

-- changeset nithiwut:004-create-asset-price

CREATE TABLE asset_price (
    id          BIGSERIAL PRIMARY KEY,
    asset_id    BIGINT NOT NULL,
    price       NUMERIC(20,8) NOT NULL,
    priced_at   TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_asset_price_asset FOREIGN KEY (asset_id) REFERENCES asset(id),
    CONSTRAINT chk_asset_price_positive CHECK (price > 0)
);

CREATE INDEX idx_asset_price_latest
    ON asset_price(asset_id, priced_at DESC, id DESC);

-- rollback DROP TABLE asset_price;
