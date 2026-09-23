-- liquibase formatted sql

-- changeset nithiwut:008-add-asset-type-and-external-id

-- Assets created before typed market data support represented stocks.
UPDATE asset SET asset_type = 'STOCK';

ALTER TABLE asset
    ALTER COLUMN asset_type SET DEFAULT 'STOCK',
    ADD COLUMN external_id VARCHAR(100),
    ADD CONSTRAINT chk_asset_type CHECK (asset_type IN ('STOCK', 'MUTUAL_FUND'));

-- rollback ALTER TABLE asset DROP CONSTRAINT chk_asset_type;
-- rollback ALTER TABLE asset DROP COLUMN external_id;
-- rollback ALTER TABLE asset ALTER COLUMN asset_type DROP DEFAULT;
