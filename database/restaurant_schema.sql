-- ============================================================
-- RESTAURANT ORDERING SYSTEM - DDL
-- PostgreSQL 15
-- ============================================================

-- ============================================================
-- 1. USERS (base table - joined table inheritance)
-- ============================================================
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)        NOT NULL,
    email       VARCHAR(100)        NOT NULL UNIQUE,
    phone       VARCHAR(20),
    password    VARCHAR(255)        NOT NULL,
    user_type   VARCHAR(10)         NOT NULL CHECK (user_type IN ('admin', 'client')),
    address     TEXT,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 2. ADMIN
-- ============================================================
CREATE TABLE admin (
    admin_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id          UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    is_active   BOOLEAN             NOT NULL DEFAULT TRUE,
    work_shift  VARCHAR(20)         CHECK (work_shift IN ('pagi', 'siang', 'malam'))
);

-- ============================================================
-- 3. CLIENT
-- ============================================================
CREATE TABLE client (
    client_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id              UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    status_member   VARCHAR(10)     NOT NULL DEFAULT 'guest' CHECK (status_member IN ('guest', 'member')),
    total_point     INT             NOT NULL DEFAULT 0
);

-- ============================================================
-- 4. MEJA
-- ============================================================
CREATE TABLE meja (
    meja_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nomor_meja      INT             NOT NULL UNIQUE,
    qr_code_url     TEXT,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by      UUID            REFERENCES admin(admin_id) ON DELETE SET NULL
);

-- ============================================================
-- 5. PROMO
-- ============================================================
CREATE TABLE promo (
    promo_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nama_promo      VARCHAR(100)    NOT NULL,
    tipe_diskon     VARCHAR(10)     NOT NULL CHECK (tipe_diskon IN ('nominal', 'persen')),
    nilai_diskon    NUMERIC(10, 2)  NOT NULL,
    tanggal_mulai   DATE            NOT NULL,
    tanggal_selesai DATE            NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    CHECK (tanggal_selesai >= tanggal_mulai)
);

-- ============================================================
-- 6. MENUS
-- ============================================================
CREATE TABLE menus (
    menu_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_name       VARCHAR(100)    NOT NULL,
    price           NUMERIC(10, 2)  NOT NULL CHECK (price >= 0),
    description     TEXT,
    category        VARCHAR(50)     NOT NULL,
    is_available    BOOLEAN         NOT NULL DEFAULT TRUE,
    image_url       TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by      UUID            REFERENCES admin(admin_id) ON DELETE SET NULL,
    updated_by      UUID            REFERENCES admin(admin_id) ON DELETE SET NULL,
    promo_id        UUID            REFERENCES promo(promo_id) ON DELETE SET NULL
);

-- ============================================================
-- 7. KERANJANG
-- ============================================================
CREATE TABLE keranjang (
    keranjang_id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID UNIQUE REFERENCES client(client_id) ON DELETE SET NULL
    -- UNIQUE karena 1 client hanya boleh punya 1 keranjang aktif
);

-- ============================================================
-- 8. DETAIL KERANJANG
-- ============================================================
CREATE TABLE detail_keranjang (
    detail_keranjang_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keranjang_id            UUID NOT NULL REFERENCES keranjang(keranjang_id) ON DELETE CASCADE,
    menu_id                 UUID NOT NULL REFERENCES menus(menu_id) ON DELETE CASCADE,
    quantity                INT  NOT NULL CHECK (quantity > 0),
    catatan                 TEXT
);

-- ============================================================
-- 9. PESANAN
-- ============================================================
CREATE TABLE pesanan (
    pesanan_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id           UUID            REFERENCES client(client_id) ON DELETE SET NULL,
    meja_id             UUID            REFERENCES meja(meja_id) ON DELETE SET NULL,
    tanggal_pesanan     TIMESTAMP       NOT NULL DEFAULT NOW(),
    is_served           BOOLEAN         NOT NULL DEFAULT FALSE,
    total_harga         NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    jumlah_dibayar      NUMERIC(10, 2),
    status              VARCHAR(20)     NOT NULL DEFAULT 'menunggu'
                            CHECK (status IN ('menunggu', 'diproses', 'siap_diantar', 'selesai', 'batal')),
    catatan_tambahan    TEXT,
    estimasi_menit      INT,
    metode_pembayaran   VARCHAR(10)     CHECK (metode_pembayaran IN ('cash', 'qris')),
    poin_digunakan      INT             NOT NULL DEFAULT 0,
    potongan_poin       NUMERIC(10, 2)  NOT NULL DEFAULT 0
);

-- ============================================================
-- 10. DETAIL PESANAN
-- ============================================================
CREATE TABLE detail_pesanan (
    detail_pesanan_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pesanan_id              UUID            NOT NULL REFERENCES pesanan(pesanan_id) ON DELETE CASCADE,
    menu_id                 UUID            NOT NULL REFERENCES menus(menu_id) ON DELETE RESTRICT,
    quantity                INT             NOT NULL CHECK (quantity > 0),
    catatan                 TEXT,
    harga_snapshot          NUMERIC(10, 2)  NOT NULL, -- harga saat transaksi, imun dari perubahan harga menu
    harga_setelah_diskon    NUMERIC(10, 2)  NOT NULL, -- = harga_snapshot jika tidak ada promo
    sub_total               NUMERIC(10, 2)  NOT NULL  -- = harga_setelah_diskon * quantity
);

-- ============================================================
-- 11. RIWAYAT PESANAN
-- ============================================================
CREATE TABLE riwayat_pesanan (
    riwayat_pesanan_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id           UUID NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    pesanan_id          UUID NOT NULL UNIQUE REFERENCES pesanan(pesanan_id) ON DELETE CASCADE,
    tanggal             TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 12. POIN TRANSAKSI
-- ============================================================
CREATE TABLE poin_transaksi (
    poin_transaksi_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id           UUID NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    pesanan_id          UUID NOT NULL REFERENCES pesanan(pesanan_id) ON DELETE CASCADE,
    jumlah_poin         INT  NOT NULL, -- positif = earn, negatif = redeem
    tipe                VARCHAR(10) NOT NULL CHECK (tipe IN ('earn', 'redeem')),
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 13. RATING
-- ============================================================
CREATE TABLE rating (
    rating_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id   UUID            NOT NULL REFERENCES client(client_id) ON DELETE CASCADE,
    menu_id     UUID            NOT NULL REFERENCES menus(menu_id) ON DELETE CASCADE,
    pesanan_id  UUID            NOT NULL REFERENCES pesanan(pesanan_id) ON DELETE CASCADE,
    bintang     SMALLINT        NOT NULL CHECK (bintang BETWEEN 1 AND 5),
    ulasan      TEXT,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    -- 1 client hanya bisa rating 1 menu dari 1 pesanan yang sama
    UNIQUE (client_id, menu_id, pesanan_id)
);

-- ============================================================
-- 14. RESTO CONFIG
-- ============================================================
CREATE TABLE resto_config (
    config_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    is_open     BOOLEAN     NOT NULL DEFAULT TRUE,
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_by  UUID        REFERENCES admin(admin_id) ON DELETE SET NULL
);

-- Seed default config
INSERT INTO resto_config (is_open) VALUES (TRUE);

-- ============================================================
-- INDEXES (optimasi query yang sering dipakai)
-- ============================================================
CREATE INDEX idx_pesanan_client_id        ON pesanan(client_id);
CREATE INDEX idx_pesanan_status           ON pesanan(status);
CREATE INDEX idx_pesanan_meja_id          ON pesanan(meja_id);
CREATE INDEX idx_detail_pesanan_pesanan   ON detail_pesanan(pesanan_id);
CREATE INDEX idx_detail_keranjang_ker     ON detail_keranjang(keranjang_id);
CREATE INDEX idx_menus_category           ON menus(category);
CREATE INDEX idx_menus_is_available       ON menus(is_available);
CREATE INDEX idx_poin_transaksi_client    ON poin_transaksi(client_id);
CREATE INDEX idx_rating_menu_id           ON rating(menu_id);