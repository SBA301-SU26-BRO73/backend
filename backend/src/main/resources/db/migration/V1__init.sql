
-- ---------------------------------------------------------------------
-- court_types  — reference catalog managed by Super Admin, no soft delete
-- ---------------------------------------------------------------------
CREATE TABLE court_types (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- subscription_plans  — catalog of platform plans, Super-Admin managed
-- is_active : hide a plan from new sign-ups without deleting it
-- ---------------------------------------------------------------------
CREATE TABLE subscription_plans (
    id            BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL UNIQUE,
    price         NUMERIC(10,2) NOT NULL,
    max_courts    INT           NOT NULL,
    max_branches  INT           NOT NULL,
    duration_days INT           NOT NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- users  — registered accounts only (guests book via bookings.guest_phone)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    phone          VARCHAR(20),
    role           VARCHAR(20)  NOT NULL
                   CHECK (role IN ('CUSTOMER', 'STAFF', 'ADMIN', 'SUPER_ADMIN')),
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING_APPROVAL'
                   CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'LOCKED')),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);

-- ---------------------------------------------------------------------
-- subscriptions  — an Admin's purchased plan; soft-deleted on cancellation
-- max_courts / max_branches: snapshot of the plan limits at purchase time
--   (preserved even if the plan catalog changes later)
-- ---------------------------------------------------------------------
CREATE TABLE subscriptions (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_id      BIGINT      NOT NULL REFERENCES users              (id) ON DELETE RESTRICT,
    plan_id       BIGINT      NOT NULL REFERENCES subscription_plans (id) ON DELETE RESTRICT,
    max_courts    INT         NOT NULL,
    max_branches  INT         NOT NULL,
    start_date    DATE        NOT NULL,
    end_date      DATE        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);

-- ---------------------------------------------------------------------
-- branches  — physical location owned by an Admin; holds bank account
-- ---------------------------------------------------------------------
CREATE TABLE branches (
    id                   BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_id             BIGINT       NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    name                 VARCHAR(150) NOT NULL,
    address              VARCHAR(255) NOT NULL,
    ward                 VARCHAR(100),
    city                 VARCHAR(100) NOT NULL,
    phone                VARCHAR(20),
    open_time            TIME         NOT NULL,
    close_time           TIME         NOT NULL,
    bank_account_number  VARCHAR(50),
    bank_account_name    VARCHAR(150),
    bank_name            VARCHAR(100),
    bank_qr_image_url    TEXT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                         CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at           TIMESTAMPTZ
);

-- ---------------------------------------------------------------------
-- courts  — a single court inside a branch
-- ---------------------------------------------------------------------
CREATE TABLE courts (
    id             BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    branch_id      BIGINT       NOT NULL REFERENCES branches    (id) ON DELETE RESTRICT,
    name           VARCHAR(150) NOT NULL,
    court_type_id  BIGINT       NOT NULL REFERENCES court_types (id) ON DELETE RESTRICT,
    description    TEXT,
    image_url      TEXT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                   CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ
);

-- ---------------------------------------------------------------------
-- time_slot_templates  — recurring 30-min priced slots per court & weekday
-- is_active : temporarily disable a slot (maintenance, holiday)
-- deleted_at: permanently remove the template
-- ---------------------------------------------------------------------
CREATE TABLE time_slot_templates (
    id           BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    court_id     BIGINT        NOT NULL REFERENCES courts (id) ON DELETE RESTRICT,
    start_time   TIME          NOT NULL,
    end_time     TIME          NOT NULL,
    price        NUMERIC(10,2) NOT NULL,
    day_of_week  SMALLINT      NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ
);

-- ---------------------------------------------------------------------
-- staff  — links a STAFF user to the branch they work at
-- ---------------------------------------------------------------------
CREATE TABLE staff (
    id          BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT      NOT NULL UNIQUE REFERENCES users    (id) ON DELETE RESTRICT,
    branch_id   BIGINT      NOT NULL        REFERENCES branches (id) ON DELETE RESTRICT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);

-- =====================================================================
-- MINH'S ENTITIES
-- =====================================================================

-- ---------------------------------------------------------------------
-- payments  — one bank-transfer record covering one or more bookings,
--   confirmed by admin/staff
-- branch_id : the branch whose bank account received the transfer. Every
--             booking sharing this payment MUST belong to a court in this
--             branch (enforced at the application layer — see schema.md).
-- amount    : actual amount transferred (may differ from the bookings' total
--             e.g. customer transferred wrong amount — useful for reconciliation)
-- Created before bookings because bookings.payment_id references it.
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id             BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    branch_id      BIGINT        NOT NULL REFERENCES branches (id) ON DELETE RESTRICT,
    amount         NUMERIC(10,2) NOT NULL,
    bill_image_url TEXT,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED')),
    confirmed_at   TIMESTAMPTZ,
    confirmed_by   BIGINT        REFERENCES users (id) ON DELETE RESTRICT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- bookings  — one order = 1+ consecutive slots on one court
-- customer_id OR guest_phone must be set (never both null)
-- payment_id : the bank transfer that paid for this booking; NULL until paid.
--   One payment can cover many bookings (1 payment : N bookings).
-- ---------------------------------------------------------------------
CREATE TABLE bookings (
    id           BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    court_id     BIGINT        NOT NULL REFERENCES courts   (id) ON DELETE RESTRICT,
    customer_id  BIGINT        REFERENCES users    (id) ON DELETE RESTRICT,
    payment_id   BIGINT        REFERENCES payments (id) ON DELETE SET NULL,
    guest_phone  VARCHAR(20),
    date         DATE          NOT NULL,
    status       VARCHAR(25)   NOT NULL DEFAULT 'PENDING_PAYMENT'
                 CHECK (status IN ('PENDING_PAYMENT', 'AWAITING_CONFIRMATION',
                                   'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED')),
    total_price  NUMERIC(10,2) NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_bookings_customer_or_guest
        CHECK (customer_id IS NOT NULL OR guest_phone IS NOT NULL)
);

-- ---------------------------------------------------------------------
-- booking_slots  — individual 30-min slots in a booking
-- court_id / booking_date: denormalized from the parent booking so a single
--   row carries (court, date, slot) — required to enforce no double-booking.
-- price_at_booking: snapshot of price when booked — preserves history
--   when admin later changes template prices
-- NOTE: cancelling a booking must DELETE its booking_slots rows to free the
--   slot (the booking row keeps the order/audit history via its status).
-- ---------------------------------------------------------------------
CREATE TABLE booking_slots (
    id               BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id       BIGINT        NOT NULL REFERENCES bookings (id) ON DELETE CASCADE,
    court_id         BIGINT        NOT NULL REFERENCES courts   (id) ON DELETE RESTRICT,
    booking_date     DATE          NOT NULL,
    slot_start       TIME          NOT NULL,
    slot_end         TIME          NOT NULL,
    price_at_booking NUMERIC(10,2) NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Hard guard against double-booking: at most one slot per court + date + start
CREATE UNIQUE INDEX uq_booking_slots_court_date_start
    ON booking_slots (court_id, booking_date, slot_start);

-- ---------------------------------------------------------------------
-- slot_holds  — temporary 5-min lock while a customer transfers payment
-- customer_id OR guest_phone identifies the holder (same rule as bookings).
-- A scheduled job deletes rows where expired_at < now(); the app should also
--   delete an expired hold for a slot before re-holding it (see UNIQUE below).
-- ---------------------------------------------------------------------
CREATE TABLE slot_holds (
    id          BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    court_id    BIGINT      NOT NULL REFERENCES courts (id) ON DELETE RESTRICT,
    customer_id BIGINT      REFERENCES users (id) ON DELETE CASCADE,
    guest_phone VARCHAR(20),
    date        DATE        NOT NULL,
    slot_start  TIME        NOT NULL,
    slot_end    TIME        NOT NULL,
    expired_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_slot_holds_customer_or_guest
        CHECK (customer_id IS NOT NULL OR guest_phone IS NOT NULL)
);

-- Hard guard: at most one live hold per court + date + start slot
CREATE UNIQUE INDEX uq_slot_holds_court_date_start
    ON slot_holds (court_id, date, slot_start);
