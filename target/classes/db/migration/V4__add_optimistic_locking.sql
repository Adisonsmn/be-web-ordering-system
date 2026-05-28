-- Penambahan kolom version untuk Optimistic Locking

ALTER TABLE client
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE meja
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
