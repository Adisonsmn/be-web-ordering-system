# 🍃 Aroma Senja — Backend

> **REST API + WebSocket Server untuk Sistem Pemesanan Mandiri Restoran berbasis QR Code**
> Dibangun dengan Spring Boot 3, PostgreSQL (Supabase), dan STOMP WebSocket untuk update pesanan real-time.

## About

**Aroma Senja Backend** adalah REST API & WebSocket server yang melayani dua aplikasi frontend: **Customer App** (untuk pemesanan mandiri oleh pelanggan via QR Code) dan **Dashboard Admin** (untuk manajemen operasional restoran). Dibangun menggunakan Java 21 dan Spring Boot 3, backend ini mengintegrasikan Supabase PostgreSQL sebagai penyimpanan data utama, Spring Security + JWT untuk otorisasi, dan SockJS/STOMP WebSocket untuk sinkronisasi pesanan real-time antara dapur, kasir, dan pelanggan.

---

## Fitur

Backend Aroma Senja menyediakan fungsionalitas bisnis lengkap melalui RESTful API dan WebSocket:

- **Autentikasi & Keamanan (JWT):** Registrasi pelanggan, login admin/member/tamu (guest), serta rotasi access token dan refresh token secara stateless.
- **Manajemen Meja & QR Code:** Mendaftarkan meja restoran, memvalidasi token QR, mengelola status meja (_occupied_/_vacant_), dan membuat tautan QR Code secara dinamis.
- **Katalog Menu & Rekomendasi:** Menyajikan katalog makanan dan minuman yang aktif, pencarian menu, filter kategori, penentuan opsi tingkat kematangan/pedas, serta rekomendasi _pairing menu_ (saling cocok dipesan bersama).
- **Keranjang Belanja:** Menyimpan item pesanan per sesi meja dalam database untuk menunjang keamanan data sebelum checkout.
- **Manajemen Pesanan:** Alur pemesanan lengkap (pengurangan stok, perhitungan diskon, penggunaan poin member, snapshot harga menu saat transaksi dibuat, cetak struk digital).
- **Real-time Event Broadcast:** Menggunakan WebSocket STOMP untuk menyiarkan pesanan baru ke dapur dan mengupdate estimasi waktu memasak ke pelanggan secara real-time.
- **Laporan & Analitik Restoran:** Menyediakan data statistik harian (KPI penjualan), tren pendapatan berkala, menu terlaris, serta ekspor berkas laporan keuangan ke format Microsoft Excel.
- **Loyalitas Poin:** Mengotomatiskan pemberian poin _earn_ (saat selesai belanja) dan _redeem_ (pemotongan poin untuk diskon belanja).

---

## Alur Data Backend

Backend menangani pertukaran data secara sinkron (HTTP REST) dan asinkron (WebSocket):

```
[Pelanggan Scan QR] ──> Validasi Token Meja (REST) ──> Mengambil Katalog Menu (REST)
                                                               │
                                                               v
[Dapur Proses Order] <── Notifikasi Order Baru (WS) <── Pembuatan Pesanan & Potong Poin (REST)
        │
        ├──> Update Status & Estimasi Menit (REST)
        │              │
        │              v
        └──> Kirim Update Real-time (WS) ──> [Pelanggan Memantau Status Di HP]
                                                               │
                                                               v
[Poin Bertambah / Struk Cetak] <── Selesai Diantar (SERVED) ──┘
```

---

## Struktur Project

Backend menggunakan arsitektur **Package by Feature** untuk memisahkan domain bisnis agar modular, aman, dan mudah dikembangkan:

```
aroma-senja-backend/
├── src/main/java/com/aromasenja/
│   ├── AromaSenjaApplication.java       # Kelas Entry Point Utama Spring Boot
│   │
│   ├── config/                          # Konfigurasi Global Aplikasi
│   │   ├── SecurityConfig.java          # Spring Security, password encoder, JWT filter
│   │   ├── WebSocketConfig.java         # Konfigurasi broker & endpoint STOMP WebSocket
│   │   ├── CorsConfig.java              # Kebijakan asal domain yang diizinkan (CORS)
│   │   └── RequestLoggingFilter.java    # Log performa HTTP request/response
│   │
│   ├── common/                          # Kelas utilitas & helper lantas domain
│   │   ├── exception/                   # Global Exception Handler & kelas exception custom
│   │   ├── response/                    # Standardisasi format JSON ApiResponse<T>
│   │   └── security/                    # Utilitas JWT (JwtService, JwtAuthFilter, UserPrincipal)
│   │
│   ├── domain/                          # Logika Bisnis Utama (Package by Feature)
│   │   ├── auth/                        # Registrasi, login, refresh token
│   │   ├── meja/                        # Data meja dan enkripsi QR token
│   │   ├── menu/                        # Katalog menu, kategori, dan pairing suggestion
│   │   ├── keranjang/                   # Keranjang belanja per sesi pelanggan
│   │   ├── pesanan/                     # Logika transaksi pesanan (domain terbesar)
│   │   ├── promo/                       # Kode promo dan kalkulator diskon
│   │   ├── rating/                      # Ulasan & penilaian bintang dari pelanggan
│   │   └── poin/                        # Mutasi poin loyalitas member
│   │
│   └── notification/                    # Pengirim Event WebSocket
│       └── NotificationService.java     # Publisher WebSocket ke admin & pelanggan
│
├── src/main/resources/
│   ├── application.yml                  # Berkas konfigurasi utama Spring Boot
│   └── db/migration/                    # Berkas SQL Flyway Database Migrations (V1 s.d V12)
```

---

## Cara Instalasi

Ikuti langkah-langkah di bawah ini untuk menyiapkan backend di lingkungan lokal Anda:

1.  **Prasyarat Perangkat Lunak:**
    - **JDK 21 (Java Development Kit)**: Versi 21 wajib terinstall (misalnya Eclipse Temurin LTS).
    - **Git**: Untuk kloning kode program.
    - _Catatan:_ Maven tidak perlu diinstall secara terpisah karena proyek sudah dilengkapi Maven Wrapper (`mvnw`).

2.  **Kloning Kode Sumber:**

    ```bash
    git clone <url-repository-anda>
    cd aroma-senja-backend
    ```

3.  **Membuat File Environment Variables (.env):**
    Salin berkas template `.env.example` menjadi `.env`:
    - **Windows (PowerShell):** `copy .env.example .env`
    - **Linux/Mac:** `cp .env.example .env`

4.  **Konfigurasi Database PostgreSQL (Supabase):**
    Buka file `.env` dan masukkan informasi kredensial database Anda (lihat bagian [Environment Variable](#-environment-variable)).

---

## Cara Menjalankan Aplikasi

1.  **Menjalankan Aplikasi (Mode Development):**
    - **Menggunakan Windows (Otomatis & Direkomendasikan):**
      Jalankan script PowerShell berikut. Script ini akan otomatis memuat variabel lingkungan dari `.env` dan menyalakan aplikasi:
      ```powershell
      powershell -ExecutionPolicy Bypass -File .\run-backend.ps1
      ```
    - **Secara Manual (Semua OS):**
      Ekspor terlebih dahulu variabel lingkungan dari file `.env` ke terminal Anda, lalu jalankan perintah:

      ```bash
      # Menggunakan Windows CMD/PowerShell
      .\mvnw.cmd spring-boot:run

      # Menggunakan Linux/Mac Terminal
      ./mvnw spring-boot:run
      ```

2.  **Verifikasi Kesehatan API:**
    Buka browser Anda dan akses:
    - Status Restoran: [http://localhost:8080/api/config](http://localhost:8080/api/config)

---

## Environment Variable

Variabel lingkungan berikut wajib dikonfigurasi pada file `.env` di folder root backend agar aplikasi dapat terhubung ke database dan mengenali token keamanan:

| Variable               | Wajib | Deskripsi                                                 | Contoh Nilai                                          |
| ---------------------- | ----- | --------------------------------------------------------- | ----------------------------------------------------- |
| `DATABASE_URL`         | Ya    | JDBC URL koneksi database PostgreSQL                      | `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres` |
| `DB_USERNAME`          | Ya    | Username superuser database PostgreSQL                    | `postgres.xxxx`                                       |
| `DB_PASSWORD`          | Ya    | Kata sandi akses database PostgreSQL                      | `KataSandiDatabaseAnda`                               |
| `JWT_SECRET`           | Ya    | Kunci rahasia minimal 32 karakter untuk tanda tangan JWT  | `asfasdfwerasdfdf`                                    |
| `QR_BASE_URL`          | Ya    | Tautan dasar web frontend yang ditanam di QR Code         | `http://localhost:5173/customer`                      |
| `CORS_ALLOWED_ORIGINS` | Ya    | Alamat frontend yang diizinkan mengakses resource backend | `http://localhost:5173`                               |

---

## Anggota Tim

Proyek ini dibangun oleh Kelompok Aroma Senja untuk Tugas Besar mata kuliah Pemrograman Berorientasi Objek (PBO):

| Nama                       | GitHub                                             | Peran                        |
| -------------------------- | -------------------------------------------------- | ---------------------------- |
| **Adison Simanullang**     | [@Adisonsmn](https://github.com/Adisonsmn)         | Backend & Database Lead      |
| **Agung Natanael Saragih** | [@agungsrgh](https://github.com/agungsrgh)         | Backend & Frontend Developer |
| **Farhan Hamzah**          | [@farhan-hamzah](https://github.com/farhan-hamzah) | Backend & Frontend Developer |
| **Nazal Putra**            | [@NazalDev](https://github.com/NazalDev)           | Backend & Frontend Developer |
| **Muhammad Huttaqi**       | [@MrTakeIt](https://github.com/MrTakeIt)           | Backend & Frontend Developer |

---

## API Endpoints

Semua endpoint API diawali dengan `/api/`. Format respons seragam:

```json
{
  "success": true,
  "message": "Pesan sukses deskriptif",
  "data": { ... }
}
```

### Public (Tanpa Autentikasi JWT)

- `POST /api/auth/login` - Masuk log akun pelanggan/admin.
- `POST /api/auth/register` - Pendaftaran akun pelanggan member.
- `POST /api/auth/refresh` - Memperbarui access token JWT yang kedaluwarsa.
- `GET /api/auth/guest` - Mendapatkan JWT sementara untuk pengguna non-member.
- `GET /api/menu` - Mendapatkan daftar menu restoran yang tersedia.
- `GET /api/meja/scan/{qrToken}` - Validasi keaktifan QR meja saat di-scan.

### Client (Butuh Token JWT - Role: CLIENT)

- `GET /api/keranjang` - Mengambil isi keranjang belanja.
- `POST /api/keranjang/items` - Menambahkan item makanan ke keranjang.
- `POST /api/pesanan` - Mengirimkan pesanan belanja untuk diproses dapur.
- `POST /api/rating` - Mengirim ulasan & bintang.

### Admin (Butuh Token JWT - Role: ADMIN)

- `GET /api/pesanan` - Mengambil semua riwayat pesanan (dukungan filter status & pagination).
- `PATCH /api/pesanan/{id}/status` - Mengubah status pesanan di dapur/kasir.
- `POST /api/menu` - CRUD item menu baru.
- `POST /api/promo` - Membuat promo baru.
- `GET /api/laporan/stats` - Statistik KPI laporan harian/bulanan.
- `GET /api/laporan/export` - Ekspor laporan keuangan ke berkas `.xlsx`.

---

## WebSocket Events

WebSocket server beroperasi di alamat `ws://localhost:8080/ws` menggunakan protokol STOMP:

- `/topic/admin/pesanan-baru` - Broadcaster pesanan baru masuk ke admin.
- `/topic/pesanan/{pesananId}` - Mengirimkan update status pesanan real-time ke pelanggan spesifik.
- `/topic/menu/availability` - Siaran perubahan ketersediaan stok menu ke seluruh pelanggan.
- `/topic/resto/status` - Siaran status operasional resto (buka/tutup) ke seluruh pelanggan.

---

<div align="center">
  <sub>Built with ☕ Java & Spring Boot by Tim Aroma Senja</sub>
</div>
