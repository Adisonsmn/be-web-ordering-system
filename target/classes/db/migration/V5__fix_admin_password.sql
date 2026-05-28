-- ============================================================
--  V5__fix_admin_password.sql — Fix hash password admin default
--
--  Hash sebelumnya di V2 ternyata tidak match dengan "Admin@2024!"
--  Hash baru di bawah ini adalah BCrypt(12) yang benar untuk "Admin@2024!"
-- ============================================================

UPDATE public.users
SET password = '$2a$12$aOIQfp8abUZaK5WVCUOQaew4GGhf53XJWEbRRPPspRUiA3axtO7cq'
WHERE email = 'admin@aromasenja.com';
