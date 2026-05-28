-- ============================================================
--  V2__seed_data.sql — Aroma Senja Backend
--  Data awal untuk development dan demo.
--
--  PASSWORD ADMIN DEFAULT: Admin@2024!
--  Hash di bawah adalah BCrypt(12) untuk "Admin@2024!"
--  GANTI hash ini jika ingin password berbeda:
--    - Jalankan: new BCryptPasswordEncoder(12).encode("passwordBaru")
--    - Atau via endpoint POST /api/auth/register lalu ubah role di DB
-- ============================================================

-- ── 1. Admin default ────────────────────────────────────────
INSERT INTO public.users (id, name, email, phone, password, user_type, created_at)
VALUES (
    gen_random_uuid(),
    'Super Admin',
    'admin@aromasenja.com',
    '081234567890',
    '$2a$12$LGKKf5CEKjeTM5bJlFmDquGvHi7OUUvBLvvgkO6fB9eICvlm2QnJW',  -- Admin@2024!
    'admin',
    now()
);

-- Buat record admin yang mengarah ke user yang baru dibuat
INSERT INTO public.admin (admin_id, id, is_active, work_shift)
SELECT
    gen_random_uuid(),
    u.id,
    true,
    'pagi'
FROM public.users u
WHERE u.email = 'admin@aromasenja.com';

-- ── 2. Konfigurasi restoran default ─────────────────────────
INSERT INTO public.resto_config (config_id, is_open, open_time, close_time, updated_at)
VALUES (
    gen_random_uuid(),
    true,
    '10:00:00',
    '22:00:00',
    now()
);

-- ── 3. Meja (10 meja: 7 Indoor, 3 Outdoor) ──────────────────
INSERT INTO public.meja (meja_id, nomor_meja, zone, is_active, is_occupied)
VALUES
    (gen_random_uuid(), 1,  'Indoor',  true, false),
    (gen_random_uuid(), 2,  'Indoor',  true, false),
    (gen_random_uuid(), 3,  'Indoor',  true, false),
    (gen_random_uuid(), 4,  'Indoor',  true, false),
    (gen_random_uuid(), 5,  'Indoor',  true, false),
    (gen_random_uuid(), 6,  'Indoor',  true, false),
    (gen_random_uuid(), 7,  'Indoor',  true, false),
    (gen_random_uuid(), 8,  'Outdoor', true, false),
    (gen_random_uuid(), 9,  'Outdoor', true, false),
    (gen_random_uuid(), 10, 'Outdoor', true, false);

-- ── 4. Menu contoh ──────────────────────────────────────────
-- Kategori: Makanan Utama, Minuman, Snack, Dessert

-- Makanan Utama
INSERT INTO public.menus (menu_id, menu_name, price, description, category, is_available, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Nasi Goreng Spesial',  35000, 'Nasi goreng dengan telur, ayam, dan kerupuk. Disajikan dengan acar segar.',    'Makanan Utama', true, now(), now()),
    (gen_random_uuid(), 'Ayam Bakar Bumbu Rujak', 42000, 'Ayam kampung bakar dengan bumbu rujak khas Aroma Senja, disajikan dengan lalapan.', 'Makanan Utama', true, now(), now()),
    (gen_random_uuid(), 'Mie Goreng Seafood',   38000, 'Mie goreng dengan udang, cumi, dan sayuran segar.',                           'Makanan Utama', true, now(), now()),
    (gen_random_uuid(), 'Gado-Gado Segar',      28000, 'Sayuran rebus segar dengan bumbu kacang khas dan kerupuk.',                    'Makanan Utama', true, now(), now()),
    (gen_random_uuid(), 'Soto Ayam Kampung',    32000, 'Soto bening dengan ayam kampung, telur, dan perkedel.',                       'Makanan Utama', true, now(), now());

-- Minuman
INSERT INTO public.menus (menu_id, menu_name, price, description, category, is_available, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Es Teh Manis',          8000,  'Teh manis dingin dengan es batu segar.',                                      'Minuman', true, now(), now()),
    (gen_random_uuid(), 'Jus Alpukat',           18000, 'Jus alpukat segar dengan susu kental manis.',                                 'Minuman', true, now(), now()),
    (gen_random_uuid(), 'Kopi Susu Aroma Senja', 22000, 'Kopi robusta lokal dengan susu fresh milk, signature drink Aroma Senja.',     'Minuman', true, now(), now()),
    (gen_random_uuid(), 'Es Jeruk Peras',        12000, 'Jeruk segar diperas langsung, disajikan dengan es.',                          'Minuman', true, now(), now()),
    (gen_random_uuid(), 'Teh Tarik',             15000, 'Teh tarik susu khas Malaysia dengan busa lembut.',                           'Minuman', true, now(), now());

-- Snack
INSERT INTO public.menus (menu_id, menu_name, price, description, category, is_available, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Pisang Goreng Crispy',  15000, 'Pisang kepok goreng dengan tepung crispy, disajikan dengan saus coklat.',     'Snack', true, now(), now()),
    (gen_random_uuid(), 'Tahu Tempe Goreng',     12000, 'Tahu dan tempe goreng renyah dengan sambal kecap.',                           'Snack', true, now(), now()),
    (gen_random_uuid(), 'Singkong Keju',         18000, 'Singkong goreng tabur keju, manis dan gurih.',                               'Snack', true, now(), now());

-- Dessert
INSERT INTO public.menus (menu_id, menu_name, price, description, category, is_available, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Es Krim Kelapa Muda',  22000, 'Es krim dengan daging kelapa muda asli dan pandan.',                          'Dessert', true, now(), now()),
    (gen_random_uuid(), 'Puding Coklat',         15000, 'Puding coklat lembut dengan saus vanilla.',                                   'Dessert', true, now(), now());

-- ============================================================
--  CATATAN PENTING UNTUK DEVELOPMENT
-- ============================================================
-- 1. Password admin default: Admin@2024!
--    Ganti segera di production dengan password yang kuat.
--
-- 2. JWT_SECRET harus di-set di environment variable.
--    Generate dengan: openssl rand -base64 64
--
-- 3. Untuk koneksi ke Supabase, set env var:
--    - DATABASE_URL=jdbc:postgresql://db.XXXX.supabase.co:5432/postgres?sslmode=require
--    - DB_USERNAME=postgres
--    - DB_PASSWORD=<your-supabase-password>
--    - APP_BASE_URL=https://your-backend-domain.com
--    - JWT_SECRET=<base64-encoded-secret-min-32-chars>
