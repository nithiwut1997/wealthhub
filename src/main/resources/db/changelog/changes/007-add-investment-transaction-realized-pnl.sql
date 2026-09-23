-- liquibase formatted sql

-- changeset nithiwut:007-add-investment-transaction-realized-pnl

ALTER TABLE investment_transaction
    ADD COLUMN realized_pnl NUMERIC(20,8);

-- rollback ALTER TABLE investment_transaction DROP COLUMN realized_pnl;
