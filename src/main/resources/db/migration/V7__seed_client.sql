-- ============================================================
--  V7__seed_client.sql — Aroma Senja Backend
--  Data awal untuk test Client (pelanggan).
--  Ini dipanggil untuk memperbaiki issue QA dimana client tidak terdaftar dengan password yang benar.
-- ============================================================

-- Upsert ke tabel users
INSERT INTO public.users (id, name, email, phone, password, user_type, created_at)
VALUES (
    gen_random_uuid(),
    'Client Member',
    'client@aromasenja.com',
    '089988776655',
    '$2a$12$r9RxIrvW.rxgVgiaVLaZm.d817K3WLYIa4Yldr2Ac7zKCu6NAMLyS',  -- Hash untuk: Client@2024!
    'client',
    now()
)
ON CONFLICT (email) DO UPDATE 
SET password = EXCLUDED.password;

-- Pastikan record client nya ada
INSERT INTO public.client (client_id, id, status_member, total_point)
SELECT
    gen_random_uuid(),
    u.id,
    'regular',
    0
FROM public.users u
WHERE u.email = 'client@aromasenja.com'
  AND NOT EXISTS (
      SELECT 1 FROM public.client c WHERE c.id = u.id
  );
