-- liquibase formatted sql

-- changeset nithiwut:003-create-holding

CREATE TABLE holding (
    id                  BIGSERIAL PRIMARY KEY,
    portfolio_id        BIGINT NOT NULL,
    asset_id            BIGINT NOT NULL,
    quantity            NUMERIC(20,8) NOT NULL,
    average_cost        NUMERIC(20,8) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_holding_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolio(id),
    CONSTRAINT fk_holding_asset FOREIGN KEY (asset_id) REFERENCES asset(id)
);

CREATE INDEX idx_holding_portfolio ON holding(portfolio_id);
CREATE INDEX idx_holding_asset ON holding(asset_id);
