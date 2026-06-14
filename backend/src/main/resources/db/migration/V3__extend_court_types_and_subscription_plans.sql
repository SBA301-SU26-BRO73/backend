-- =========================================================
-- court_types: add display/filter columns + soft delete
-- =========================================================
ALTER TABLE court_types
    ADD COLUMN name_en     VARCHAR(100),
    ADD COLUMN description TEXT,
    ADD COLUMN icon        VARCHAR(50),
    ADD COLUMN color       VARCHAR(20),
    ADD COLUMN active      BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at  TIMESTAMPTZ;

-- Replace the table-level UNIQUE with a partial index so soft-deleted
-- names do not block re-use of the same name later.
ALTER TABLE court_types DROP CONSTRAINT court_types_name_key;
CREATE UNIQUE INDEX uq_court_types_name_active
    ON court_types (name) WHERE deleted_at IS NULL;

-- =========================================================
-- subscription_plans: replace single price/duration with
-- monthly/yearly pricing, add display fields + soft delete
-- =========================================================
ALTER TABLE subscription_plans
    DROP COLUMN price,
    DROP COLUMN duration_days;

ALTER TABLE subscription_plans
    ADD COLUMN tagline       VARCHAR(255),
    ADD COLUMN monthly_price BIGINT,
    ADD COLUMN yearly_price  BIGINT,
    ADD COLUMN color         VARCHAR(20),
    ADD COLUMN popular       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at    TIMESTAMPTZ;

ALTER TABLE subscription_plans DROP CONSTRAINT subscription_plans_name_key;
CREATE UNIQUE INDEX uq_subscription_plans_name_active
    ON subscription_plans (name) WHERE deleted_at IS NULL;

-- =========================================================
-- plan_features: feature bullet points per plan
-- =========================================================
CREATE TABLE plan_features (
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_id  BIGINT NOT NULL REFERENCES subscription_plans(id) ON DELETE CASCADE,
    feature  TEXT   NOT NULL
);
