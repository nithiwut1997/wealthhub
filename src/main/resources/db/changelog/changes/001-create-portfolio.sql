-- liquibase formatted sql

-- changeset nithiwut:001-create-portfolio

CREATE TABLE portfolio (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    base_currency   VARCHAR(3) NOT NULL DEFAULT 'THB',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_portfolio_name ON portfolio(name);
