-- ============================================================
--  V9__allow_guest_rating.sql
--  Memperbolehkan tamu (Guest) memberikan rating tanpa client_id
-- ============================================================

ALTER TABLE public.rating ALTER COLUMN client_id DROP NOT NULL;
