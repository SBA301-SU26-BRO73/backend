-- ---------------------------------------------------------------------
-- V2 — add QR check-in token to bookings
-- checkin_code  : UUID token encoded in the booking QR, set when a booking
--                 reaches CONFIRMED. Staff verify check-in looks up by this.
-- checked_in_at : audit timestamp set when staff checks the booking in.
-- ---------------------------------------------------------------------
ALTER TABLE bookings ADD COLUMN checkin_code  VARCHAR(36);
ALTER TABLE bookings ADD COLUMN checked_in_at TIMESTAMPTZ;

-- One QR token per booking; multiple NULLs allowed (unpaid bookings have none yet)
CREATE UNIQUE INDEX uq_bookings_checkin_code ON bookings (checkin_code);

-- Backfill so existing confirmed/checked-in/completed bookings are checkable
UPDATE bookings
   SET checkin_code = gen_random_uuid()::text
 WHERE status IN ('CONFIRMED', 'CHECKED_IN', 'COMPLETED')
   AND checkin_code IS NULL;
