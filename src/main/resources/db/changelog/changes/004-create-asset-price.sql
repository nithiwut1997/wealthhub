-- liquibase formatted sql

-- changeset nithiwut:004-create-asset-price

CREATE TABLE asset_price (
    asset_id    BIGINT PRIMARY KEY,
    asset_price NUMERIC(20,8) NOT NULL,
    price_date  DATE NOT NULL,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_asset_price_asset FOREIGN KEY (asset_id) REFERENCES asset(id)
);