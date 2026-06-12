-- ============================================================
--  V8__add_spice_doneness_options.sql
--  Menambahkan opsi kepedasan dan kematangan ke menu yang relevan
-- ============================================================

-- Nasi Goreng Spesial — kepedasan wajib, kematangan tidak relevan
UPDATE public.menus
SET spice_options = ARRAY['Tidak Pedas', 'Pedas Sedang', 'Pedas', 'Extra Pedas'],
    hero_image_url = image_url,
    long_description = description
WHERE menu_name = 'Nasi Goreng Spesial';

-- Ayam Bakar Bumbu Rujak — kepedasan wajib + kematangan opsional
UPDATE public.menus
SET spice_options = ARRAY['Tidak Pedas', 'Sedang', 'Pedas', 'Extra Pedas'],
    show_doneness = true,
    doneness_options = ARRAY['Setengah Matang', 'Matang', 'Matang Sempurna'],
    hero_image_url = image_url,
    long_description = description
WHERE menu_name = 'Ayam Bakar Bumbu Rujak';

-- Mie Goreng Seafood — kepedasan wajib
UPDATE public.menus
SET spice_options = ARRAY['Tidak Pedas', 'Pedas Sedang', 'Pedas'],
    hero_image_url = image_url,
    long_description = description
WHERE menu_name = 'Mie Goreng Seafood';

-- Soto Ayam Kampung — kepedasan opsional
UPDATE public.menus
SET spice_options = ARRAY['Tidak Pedas', 'Sedang', 'Pedas'],
    hero_image_url = image_url,
    long_description = description
WHERE menu_name = 'Soto Ayam Kampung';

-- Gado-Gado Segar — tanpa kepedasan, tanpa kematangan (default behavior)
-- Tidak perlu update, sudah null = tidak ditampilkan
