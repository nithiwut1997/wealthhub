-- liquibase formatted sql

-- changeset nithiwut:006-add-holding-portfolio-asset-unique-constraint

ALTER TABLE holding
    ADD CONSTRAINT uk_holding_portfolio_asset UNIQUE (portfolio_id, asset_id);

-- rollback ALTER TABLE holding DROP CONSTRAINT uk_holding_portfolio_asset;
