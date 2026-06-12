CREATE TABLE meja_session (
    session_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meja_id      UUID NOT NULL REFERENCES meja(meja_id) ON DELETE CASCADE,
    device_token VARCHAR(64) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    expired_at   TIMESTAMP NOT NULL,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX uq_meja_session_active ON meja_session(meja_id) WHERE is_active = TRUE;
CREATE INDEX idx_meja_session_meja_id ON meja_session(meja_id);
CREATE INDEX idx_meja_session_device_token ON meja_session(device_token);
