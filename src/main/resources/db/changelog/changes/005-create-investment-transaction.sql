-- liquibase formatted sql

-- changeset nithiwut:005-create-investment-transaction

CREATE TABLE investment_transaction (
    id                  BIGSERIAL PRIMARY KEY,
    portfolio_id        BIGINT NOT NULL,
    asset_id            BIGINT NOT NULL,
    transaction_type    VARCHAR(10) NOT NULL,
    quantity            NUMERIC(20,8) NOT NULL,
    price               NUMERIC(20,8) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_investment_transaction_portfolio
        FOREIGN KEY (portfolio_id) REFERENCES portfolio(id),
    CONSTRAINT fk_investment_transaction_asset
        FOREIGN KEY (asset_id) REFERENCES asset(id),
    CONSTRAINT chk_investment_transaction_type
        CHECK (transaction_type IN ('BUY', 'SELL')),
    CONSTRAINT chk_investment_transaction_quantity CHECK (quantity > 0),
    CONSTRAINT chk_investment_transaction_price CHECK (price > 0)
);

CREATE INDEX idx_investment_transaction_portfolio_history
    ON investment_transaction(portfolio_id, created_at DESC, id DESC);
CREATE INDEX idx_investment_transaction_asset ON investment_transaction(asset_id);

-- rollback DROP TABLE investment_transaction;
