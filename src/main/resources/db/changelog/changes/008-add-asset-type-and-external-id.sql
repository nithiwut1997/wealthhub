-- liquibase formatted sql

-- changeset nithiwut:008-add-asset-type-and-external-id

-- asset_type has been NOT NULL since table creation. Preserve all existing
-- values rather than silently reclassifying assets; the constraint below will
-- surface any unsupported legacy value for explicit data cleanup.

ALTER TABLE asset
    ALTER COLUMN asset_type SET DEFAULT 'STOCK',
    ADD COLUMN external_id VARCHAR(100),
    ADD CONSTRAINT chk_asset_type CHECK (asset_type IN ('STOCK', 'MUTUAL_FUND'));

-- rollback ALTER TABLE asset DROP CONSTRAINT chk_asset_type;
-- rollback ALTER TABLE asset DROP COLUMN external_id;
-- rollback ALTER TABLE asset ALTER COLUMN asset_type DROP DEFAULT;
