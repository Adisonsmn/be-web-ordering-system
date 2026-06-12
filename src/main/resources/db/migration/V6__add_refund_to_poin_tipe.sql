-- V6: Tambah 'refund' ke CHECK constraint tipe poin_transaksi.
-- Saat pesanan di-cancel dan poin sudah digunakan, system melakukan refund poin.
-- Tanpa constraint ini, cancelPesanan() akan error saat menyimpan TipePoin.REFUND.

ALTER TABLE poin_transaksi DROP CONSTRAINT IF EXISTS poin_transaksi_tipe_check;

-- Buat ulang constraint menambahkan 'refund' sebagai tipe yang valid
ALTER TABLE poin_transaksi ADD CONSTRAINT poin_transaksi_tipe_check
    CHECK (tipe IN ('earn', 'redeem', 'refund'));
