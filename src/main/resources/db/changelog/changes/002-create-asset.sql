-- liquibase formatted sql

-- changeset nithiwut:002-create-asset

CREATE TABLE asset (
    id              BIGSERIAL PRIMARY KEY,
    symbol          VARCHAR(30) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    market          VARCHAR(20) NOT NULL,
    asset_type      VARCHAR(20) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_asset_symbol_market ON asset(symbol, market);
