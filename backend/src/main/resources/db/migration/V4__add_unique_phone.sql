-- Ensure phone numbers are unique across all users (nullable — NULL is not considered duplicate)
CREATE UNIQUE INDEX uq_users_phone
    ON users (phone) WHERE phone IS NOT NULL AND deleted_at IS NULL;
