ALTER TABLE users ADD COLUMN full_name VARCHAR(150);

CREATE TABLE user_documents (
    id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    url        TEXT        NOT NULL,
    doc_type   VARCHAR(20) NOT NULL CHECK (doc_type IN ('LEGAL', 'COURT_IMAGE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
