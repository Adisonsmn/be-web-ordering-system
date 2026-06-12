-- ============================================================
--  V1__init_schema.sql — Aroma Senja Backend
--  Initial schema — semua tabel dibuat dalam satu migration.
--  Urutan: parent table dulu, kemudian child (FK constraint).
-- ============================================================

-- ── 1. USERS ────────────────────────────────────────────────
-- Tabel induk untuk semua user (admin & client)
CREATE TABLE public.users (
    id           uuid        NOT NULL DEFAULT gen_random_uuid(),
    name         varchar     NOT NULL,
    email        varchar     NOT NULL,
    phone        varchar,
    password     varchar     NOT NULL,   -- BCrypt hash, bukan plain text
    user_type    varchar     NOT NULL
                 CHECK (user_type IN ('admin', 'client')),
    created_at   timestamp   NOT NULL DEFAULT now(),
    avatar_url   varchar,
    is_active    boolean     NOT NULL DEFAULT true,
    CONSTRAINT users_pkey        PRIMARY KEY (id),
    CONSTRAINT users_email_uniq  UNIQUE (email)
);

-- ── 2. CLIENT ───────────────────────────────────────────────
-- Profil tambahan khusus pelanggan
CREATE TABLE public.client (
    client_id     uuid     NOT NULL DEFAULT gen_random_uuid(),
    id            uuid     UNIQUE,         -- nullable: guest tidak punya users.id
    status_member varchar  NOT NULL DEFAULT 'regular'
                  CHECK (status_member IN ('guest', 'regular', 'premium')),
    total_point   integer  NOT NULL DEFAULT 0,
    CONSTRAINT client_pkey    PRIMARY KEY (client_id),
    CONSTRAINT client_id_fkey FOREIGN KEY (id) REFERENCES public.users (id)
);

-- ── 3. ADMIN ────────────────────────────────────────────────
-- Profil tambahan khusus operator restoran
CREATE TABLE public.admin (
    admin_id   uuid     NOT NULL DEFAULT gen_random_uuid(),
    id         uuid     NOT NULL UNIQUE,
    is_active  boolean  NOT NULL DEFAULT true,
    work_shift varchar
               CHECK (work_shift IN ('pagi', 'siang', 'malam')),
    CONSTRAINT admin_pkey    PRIMARY KEY (admin_id),
    CONSTRAINT admin_id_fkey FOREIGN KEY (id) REFERENCES public.users (id)
);

-- ── 4. REFRESH_TOKEN ────────────────────────────────────────
-- Stateful refresh token — support revoke/logout
CREATE TABLE public.refresh_token (
    token_id    uuid      NOT NULL DEFAULT gen_random_uuid(),
    user_id     uuid      NOT NULL,
    token       varchar(2048) NOT NULL UNIQUE,
    expires_at  timestamp NOT NULL,
    is_revoked  boolean   NOT NULL DEFAULT false,
    created_at  timestamp NOT NULL DEFAULT now(),
    CONSTRAINT refresh_token_pkey      PRIMARY KEY (token_id),
    CONSTRAINT refresh_token_user_fkey FOREIGN KEY (user_id) REFERENCES public.users (id)
);

-- ── 5. RESTO_CONFIG ─────────────────────────────────────────
-- Konfigurasi global restoran (satu baris saja)
CREATE TABLE public.resto_config (
    config_id  uuid    NOT NULL DEFAULT gen_random_uuid(),
    nama_restoran varchar NOT NULL DEFAULT 'Aroma Senja',
    tagline    varchar NOT NULL DEFAULT 'Cita Rasa Nusantara',
    alamat     text,
    telepon    varchar,
    email      varchar,
    instagram  varchar,
    is_open    boolean NOT NULL DEFAULT true,
    open_time  time    NOT NULL DEFAULT '08:00:00',
    close_time time    NOT NULL DEFAULT '22:00:00',
    updated_at timestamp NOT NULL DEFAULT now(),
    updated_by uuid,
    CONSTRAINT resto_config_pkey       PRIMARY KEY (config_id),
    CONSTRAINT resto_config_admin_fkey FOREIGN KEY (updated_by) REFERENCES public.admin (admin_id)
);

-- ── 6. MEJA ─────────────────────────────────────────────────
CREATE TABLE public.meja (
    meja_id     uuid    NOT NULL DEFAULT gen_random_uuid(),
    nomor_meja  integer NOT NULL,
    qr_code_url text,
    zone        varchar NOT NULL DEFAULT 'Indoor'
                CHECK (zone IN ('Indoor', 'Outdoor')),
    is_active   boolean NOT NULL DEFAULT true,
    is_occupied boolean NOT NULL DEFAULT false,
    created_by  uuid,
    CONSTRAINT meja_pkey          PRIMARY KEY (meja_id),
    CONSTRAINT meja_nomor_uniq    UNIQUE (nomor_meja),
    CONSTRAINT meja_admin_fkey    FOREIGN KEY (created_by) REFERENCES public.admin (admin_id)
);

-- ── 7. PROMO ────────────────────────────────────────────────
-- Didefinisikan sebelum menus karena menus punya FK ke promo
CREATE TABLE public.promo (
    promo_id        uuid    NOT NULL DEFAULT gen_random_uuid(),
    nama_promo      varchar NOT NULL,
    tipe_diskon     varchar NOT NULL
                    CHECK (tipe_diskon IN ('nominal', 'persen')),
    nilai_diskon    numeric NOT NULL,
    tanggal_mulai   date    NOT NULL,
    tanggal_selesai date    NOT NULL,
    target_category varchar DEFAULT NULL,
    is_active       boolean NOT NULL DEFAULT true,
    image_url       varchar,
    tag             varchar,
    description     text,
    CONSTRAINT promo_pkey PRIMARY KEY (promo_id)
);

-- ── 8. MENUS ────────────────────────────────────────────────
CREATE TABLE public.menus (
    menu_id          uuid    NOT NULL DEFAULT gen_random_uuid(),
    menu_name        varchar NOT NULL,
    price            numeric NOT NULL CHECK (price >= 0),
    description      text,
    category         varchar NOT NULL,
    is_available     boolean NOT NULL DEFAULT true,
    is_active        boolean NOT NULL DEFAULT true,
    image_url        text,
    created_at       timestamp NOT NULL DEFAULT now(),
    updated_at       timestamp NOT NULL DEFAULT now(),
    created_by       uuid,
    updated_by       uuid,
    promo_id         uuid,
    title_line1      varchar,
    title_line2      varchar,
    long_description text,
    hero_image_url   varchar,
    show_doneness    boolean DEFAULT false,
    doneness_options text[],
    spice_options    text[],
    CONSTRAINT menus_pkey            PRIMARY KEY (menu_id),
    CONSTRAINT menus_created_by_fkey FOREIGN KEY (created_by)  REFERENCES public.admin (admin_id),
    CONSTRAINT menus_updated_by_fkey FOREIGN KEY (updated_by)  REFERENCES public.admin (admin_id),
    CONSTRAINT menus_promo_fkey      FOREIGN KEY (promo_id)    REFERENCES public.promo (promo_id)
);

-- ── 9. MENU_PAIRINGS ────────────────────────────────────────
-- "Sering dipesan bersama" — relasi many-to-many menu ke menu
CREATE TABLE public.menu_pairings (
    menu_id         uuid      NOT NULL,
    pairing_menu_id uuid      NOT NULL,
    created_at      timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT menu_pairings_pkey      PRIMARY KEY (menu_id, pairing_menu_id),
    CONSTRAINT menu_pairings_menu_fkey FOREIGN KEY (menu_id)         REFERENCES public.menus (menu_id),
    CONSTRAINT menu_pairings_pair_fkey FOREIGN KEY (pairing_menu_id) REFERENCES public.menus (menu_id)
);

-- ── 10. KERANJANG ───────────────────────────────────────────
CREATE TABLE public.keranjang (
    keranjang_id uuid NOT NULL DEFAULT gen_random_uuid(),
    client_id    uuid UNIQUE,              -- null jika guest (Pure JWT)
    session_id   uuid,                     -- identifier untuk guest session
    CONSTRAINT keranjang_pkey        PRIMARY KEY (keranjang_id),
    CONSTRAINT keranjang_client_fkey FOREIGN KEY (client_id) REFERENCES public.client (client_id),
    -- Pastikan minimal satu dari client_id atau session_id terisi
    CONSTRAINT keranjang_owner_check CHECK (
        client_id IS NOT NULL OR session_id IS NOT NULL
    )
);

-- ── 11. DETAIL_KERANJANG ────────────────────────────────────
CREATE TABLE public.detail_keranjang (
    detail_keranjang_id uuid    NOT NULL DEFAULT gen_random_uuid(),
    keranjang_id        uuid    NOT NULL,
    menu_id             uuid    NOT NULL,
    quantity            integer NOT NULL CHECK (quantity > 0),
    catatan             text,
    CONSTRAINT detail_keranjang_pkey      PRIMARY KEY (detail_keranjang_id),
    CONSTRAINT detail_keranjang_cart_fkey FOREIGN KEY (keranjang_id) REFERENCES public.keranjang (keranjang_id) ON DELETE CASCADE,
    CONSTRAINT detail_keranjang_menu_fkey FOREIGN KEY (menu_id)      REFERENCES public.menus (menu_id)
);

-- ── 12. PESANAN ─────────────────────────────────────────────
CREATE TABLE public.pesanan (
    pesanan_id        uuid      NOT NULL DEFAULT gen_random_uuid(),
    kode_pesanan      varchar   NOT NULL UNIQUE,   -- contoh: AR-2048, generated di aplikasi
    client_id         uuid,                         -- nullable: guest boleh pesan
    meja_id           uuid,
    tanggal_pesanan   timestamp NOT NULL DEFAULT now(),
    is_served         boolean   NOT NULL DEFAULT false,
    total_harga       numeric   NOT NULL DEFAULT 0,
    jumlah_dibayar    numeric,
    status            varchar   NOT NULL DEFAULT 'new'
                      CHECK (status IN ('new', 'preparing', 'ready', 'served', 'cancelled')),
    catatan_dapur     text,
    estimasi_menit    integer   DEFAULT 0,
    metode_pembayaran varchar
                      CHECK (metode_pembayaran IN ('cash', 'qris')),
    poin_digunakan    integer   NOT NULL DEFAULT 0,
    potongan_poin     numeric   NOT NULL DEFAULT 0,
    CONSTRAINT pesanan_pkey        PRIMARY KEY (pesanan_id),
    CONSTRAINT pesanan_client_fkey FOREIGN KEY (client_id) REFERENCES public.client (client_id),
    CONSTRAINT pesanan_meja_fkey   FOREIGN KEY (meja_id)   REFERENCES public.meja (meja_id)
);

-- ── 13. DETAIL_PESANAN ──────────────────────────────────────
CREATE TABLE public.detail_pesanan (
    detail_pesanan_id    uuid    NOT NULL DEFAULT gen_random_uuid(),
    pesanan_id           uuid    NOT NULL,
    menu_id              uuid    NOT NULL,
    quantity             integer NOT NULL CHECK (quantity > 0),
    catatan              text,
    harga_snapshot       numeric NOT NULL,   -- harga saat pesanan dibuat (snapshot)
    harga_setelah_diskon numeric NOT NULL,
    sub_total            numeric NOT NULL,
    CONSTRAINT detail_pesanan_pkey       PRIMARY KEY (detail_pesanan_id),
    CONSTRAINT detail_pesanan_order_fkey FOREIGN KEY (pesanan_id) REFERENCES public.pesanan (pesanan_id) ON DELETE CASCADE,
    CONSTRAINT detail_pesanan_menu_fkey  FOREIGN KEY (menu_id)    REFERENCES public.menus (menu_id)
);

-- ── 14. RATING ──────────────────────────────────────────────
CREATE TABLE public.rating (
    rating_id      uuid     NOT NULL DEFAULT gen_random_uuid(),
    client_id      uuid     NOT NULL,
    menu_id        uuid,                              -- nullable: null jika is_overall = true
    pesanan_id     uuid     NOT NULL,
    bintang        smallint NOT NULL CHECK (bintang >= 1 AND bintang <= 5),
    ulasan         text,
    is_overall     boolean  NOT NULL DEFAULT false,
    is_public      boolean  NOT NULL DEFAULT true,
    created_at     timestamp NOT NULL DEFAULT now(),
    CONSTRAINT rating_pkey         PRIMARY KEY (rating_id),
    CONSTRAINT rating_client_fkey  FOREIGN KEY (client_id)  REFERENCES public.client (client_id),
    CONSTRAINT rating_menu_fkey    FOREIGN KEY (menu_id)    REFERENCES public.menus (menu_id),
    CONSTRAINT rating_pesanan_fkey FOREIGN KEY (pesanan_id) REFERENCES public.pesanan (pesanan_id),
    CONSTRAINT rating_overall_check CHECK (
        (is_overall = true  AND menu_id IS NULL) OR
        (is_overall = false AND menu_id IS NOT NULL)
    )
);

-- ── 15. POIN_TRANSAKSI ──────────────────────────────────────
CREATE TABLE public.poin_transaksi (
    poin_transaksi_id uuid      NOT NULL DEFAULT gen_random_uuid(),
    client_id         uuid      NOT NULL,
    pesanan_id        uuid      NOT NULL,
    jumlah_poin       integer   NOT NULL,
    tipe              varchar   NOT NULL
                      CHECK (tipe IN ('earn', 'redeem')),
    created_at        timestamp NOT NULL DEFAULT now(),
    CONSTRAINT poin_transaksi_pkey         PRIMARY KEY (poin_transaksi_id),
    CONSTRAINT poin_transaksi_client_fkey  FOREIGN KEY (client_id)  REFERENCES public.client (client_id),
    CONSTRAINT poin_transaksi_pesanan_fkey FOREIGN KEY (pesanan_id) REFERENCES public.pesanan (pesanan_id)
);

-- ============================================================
--  INDEX — untuk query yang sering dipakai
-- ============================================================

-- Cari pesanan berdasarkan client (riwayat pesanan)
CREATE INDEX idx_pesanan_client_id   ON public.pesanan (client_id);
-- Filter pesanan berdasarkan status (kanban dashboard)
CREATE INDEX idx_pesanan_status      ON public.pesanan (status);
-- Filter pesanan berdasarkan meja (real-time dashboard)
CREATE INDEX idx_pesanan_meja_id     ON public.pesanan (meja_id);
-- Cari menu berdasarkan kategori (tab katalog)
CREATE INDEX idx_menus_category      ON public.menus (category);
-- Filter menu aktif/tersedia
CREATE INDEX idx_menus_available     ON public.menus (is_available);
-- Cari rating publik per menu (untuk avg bintang di katalog)
CREATE INDEX idx_rating_menu_public  ON public.rating (menu_id, is_public) WHERE is_public = true;
-- Lookup refresh token saat validasi
CREATE INDEX idx_refresh_token_token ON public.refresh_token (token);
-- Cari keranjang guest berdasarkan session
CREATE INDEX idx_keranjang_session   ON public.keranjang (session_id) WHERE session_id IS NOT NULL;
