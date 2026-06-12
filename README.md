# 🍃 Aroma Senja — Backend

> **REST API + WebSocket Server untuk Sistem Pemesanan Mandiri Restoran berbasis QR Code**
> Dibangun dengan Spring Boot 3, PostgreSQL (Supabase), dan STOMP WebSocket untuk update pesanan real-time.

---

## 📋 Daftar Isi

- [Tentang Project](#-tentang-project)
- [Tech Stack](#-tech-stack)
- [Arsitektur & Struktur Package](#-arsitektur--struktur-package)
- [Prasyarat](#-prasyarat)
- [Cara Menjalankan (Development)](#-cara-menjalankan-development)
- [Environment Variables](#-environment-variables)
- [API Endpoints](#-api-endpoints)
- [WebSocket Events](#-websocket-events)
- [Database & Migrasi](#-database--migrasi)
- [Autentikasi & Otorisasi](#-autentikasi--otorisasi)
- [Perintah yang Tersedia](#-perintah-yang-tersedia)
- [Unit Testing](#-unit-testing)
- [Panduan Kontribusi](#-panduan-kontribusi)
- [Deployment](#-deployment)

---

## 🍽️ Tentang Project

**Aroma Senja Backend** adalah REST API server yang melayani dua aplikasi frontend:
- **Customer App** — pelanggan pesan menu via QR Code
- **Dashboard Admin** — operator kelola pesanan, menu, promo, meja, laporan

Server juga menyediakan **WebSocket (STOMP)** untuk notifikasi real-time status pesanan antara dapur ↔ pelanggan ↔ admin.

### Alur Data Utama
```
Pelanggan scan QR → API validasi meja → Ambil katalog menu
    → Buat pesanan → Notifikasi WebSocket ke dapur
    → Admin update status → Notifikasi WebSocket ke pelanggan
    → Pesanan SERVED → Poin loyalitas di-update
```

---

## 🛠️ Tech Stack

| Kategori | Teknologi | Versi |
|----------|-----------|-------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.3.4 |
| Database | PostgreSQL (Supabase) | 15+ |
| ORM | Spring Data JPA + Hibernate | 6.x |
| Migrasi DB | Flyway | 10.x |
| Autentikasi | JWT (JJWT) | 0.12.6 |
| Keamanan | Spring Security | 6.x |
| WebSocket | Spring WebSocket + STOMP | - |
| Mapping | MapStruct | 1.6.2 |
| Boilerplate | Lombok | 1.18.46 |
| QR Code | ZXing (Google) | 3.5.3 |
| Excel Export | Apache POI | 5.3.0 |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.6.0 |
| Build Tool | Maven Wrapper (`mvnw`) | 3.9.9 |
| Testing | JUnit 5 + Mockito + H2 | - |

---

## 📁 Arsitektur & Struktur Package

Menggunakan arsitektur **Package by Feature** dengan layer yang ketat:

```
Controller → Service (interface) → ServiceImpl → Repository
```

```
src/main/java/com/aromasenja/
│
├── AromaSenjaApplication.java       # Entry point
│
├── config/                          # Konfigurasi global
│   ├── SecurityConfig.java          # Spring Security & JWT filter
│   ├── WebSocketConfig.java         # STOMP WebSocket broker
│   ├── CorsConfig.java              # Allowed origins
│   └── RequestLoggingFilter.java    # Log semua request/response
│
├── common/                          # Shared utilities lintas domain
│   ├── exception/                   # GlobalExceptionHandler, custom exceptions
│   ├── response/                    # ApiResponse<T> wrapper
│   ├── security/                    # JwtService, JwtAuthFilter, UserPrincipal
│   └── util/                        # Helper functions
│
├── domain/                          # Core business features
│   ├── auth/          → Login, Register, Refresh Token, Update Profil
│   ├── user/          → Entitas Client & Admin
│   ├── meja/          → Manajemen meja & generate QR Code
│   ├── menu/          → Katalog menu & pairing suggestion
│   ├── keranjang/     → Keranjang belanja per sesi meja
│   ├── pesanan/       → Pembuatan & manajemen pesanan (domain terbesar)
│   ├── promo/         → Promo & diskon
│   ├── rating/        → Ulasan & rating menu
│   ├── poin/          → Program loyalitas poin
│   ├── laporan/       → Statistik dashboard & export Excel
│   └── config_resto/  → Pengaturan restoran & profil admin
│
└── notification/                    # WebSocket event publisher
    ├── NotificationService.java     # Interface publish WS events
    ├── WebSocketController.java     # Handle subscribe request dari client
    └── payload/                     # Record classes untuk WS payload

src/main/resources/
├── application.yml                  # Konfigurasi utama + profil dev/prod
└── db/migration/                    # Flyway migration files (V1__ - V12__)
```

---

## ✅ Prasyarat

| Tools | Versi Minimum | Cek dengan |
|-------|---------------|------------|
| [JDK (Java)](https://adoptium.net) | **21** (wajib, bukan 17 atau 11) | `java -version` |
| [Git](https://git-scm.com) | - | `git -v` |
| Akses PostgreSQL | - | Minta ke ketua tim |

> ⚠️ **Maven tidak perlu diinstall terpisah.** Project sudah menggunakan **Maven Wrapper** (`mvnw.cmd` di Windows, `mvnw` di Linux/Mac) yang otomatis download Maven jika belum ada.

### Install JDK 21

Download dari [https://adoptium.net](https://adoptium.net) → pilih **Temurin 21 (LTS)**.

Setelah install, verifikasi:
```bash
java -version
# output harus: openjdk version "21.x.x"
```

---

## 🚀 Cara Menjalankan (Development)

### Langkah 1 — Clone Repository

```bash
git clone https://github.com/<username>/<nama-repo>.git
cd <nama-repo>/aroma-senja-backend
```

### Langkah 2 — Setup Environment Variables

Salin file contoh:
```bash
# Windows
copy .env.example .env

# Linux/Mac
cp .env.example .env
```

Buka file `.env` dan isi semua nilai:
```env
DATABASE_URL=jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your_database_password
JWT_SECRET=your_jwt_secret_min_32_chars_random_string
QR_BASE_URL=http://localhost:5173/customer
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

> 📌 Nilai `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` minta ke ketua tim yang punya akses Supabase.

### Langkah 3 — Jalankan Backend

**Windows (Cara Termudah — pakai script otomatis):**
```powershell
.\run-backend.ps1
```
Script ini otomatis load `.env` dan jalankan Spring Boot.

**Windows (Manual):**
```powershell
# Load env dulu secara manual, lalu:
.\mvnw.cmd spring-boot:run
```

**Linux / Mac:**
```bash
# Export env variables dulu
export DATABASE_URL="jdbc:postgresql://..."
export DB_USERNAME="postgres"
export DB_PASSWORD="..."
export JWT_SECRET="..."
export QR_BASE_URL="http://localhost:5173/customer"
export CORS_ALLOWED_ORIGINS="http://localhost:5173"

# Jalankan
./mvnw spring-boot:run
```

Backend akan berjalan di: **[http://localhost:8080](http://localhost:8080)**

### Langkah 4 — Verifikasi

Buka browser atau Postman, akses:
```
GET http://localhost:8080/api/config
```
Harus return data konfigurasi restoran → berarti server berjalan normal.

**Swagger UI (Dokumentasi API interaktif):**
```
http://localhost:8080/swagger-ui.html
```

---

## 🔑 Environment Variables

| Variable | Wajib | Contoh | Deskripsi |
|----------|-------|--------|-----------|
| `DATABASE_URL` | ✅ | `jdbc:postgresql://host:5432/postgres?sslmode=require` | JDBC URL ke PostgreSQL Supabase |
| `DB_USERNAME` | ✅ | `postgres` | Username database |
| `DB_PASSWORD` | ✅ | `yourpassword` | Password database |
| `JWT_SECRET` | ✅ | `min32charsrandomsecretkey123456` | Secret key untuk sign JWT (min 32 karakter) |
| `QR_BASE_URL` | ✅ | `http://localhost:5173/customer` | Base URL yang di-embed di QR Code meja |
| `CORS_ALLOWED_ORIGINS` | ✅ | `http://localhost:5173` | Domain frontend yang diizinkan (pisahkan koma jika lebih dari 1) |

> 🔒 File `.env` **JANGAN di-commit ke Git** — sudah ada di `.gitignore`.

---

## 📡 API Endpoints

Semua endpoint diawali `/api/`. Format response selalu:
```json
{
  "success": true,
  "message": "Pesan deskriptif",
  "data": { }
}
```

### 🔓 Public (Tidak Perlu Token)

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `POST` | `/api/auth/login` | Login customer/admin |
| `POST` | `/api/auth/register` | Registrasi pelanggan baru |
| `POST` | `/api/auth/refresh` | Refresh access token |
| `GET` | `/api/auth/guest` | Buat token tamu (guest) |
| `GET` | `/api/menu` | Ambil semua menu aktif |
| `GET` | `/api/menu/{id}` | Detail satu menu |
| `GET` | `/api/promo` | Promo yang sedang aktif |
| `GET` | `/api/config` | Status & info restoran |
| `GET` | `/api/meja/scan/{qrToken}` | Validasi QR Code meja |

### 🔐 Client (Butuh Token JWT — Role: CLIENT)

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `GET` | `/api/auth/me` | Ambil profil sendiri |
| `PUT` | `/api/auth/me` | Update profil (nama, telepon, avatar) |
| `GET` | `/api/keranjang` | Ambil isi keranjang |
| `POST` | `/api/keranjang/items` | Tambah item ke keranjang |
| `PUT` | `/api/keranjang/items/{id}` | Update quantity item |
| `DELETE` | `/api/keranjang/items/{id}` | Hapus item dari keranjang |
| `DELETE` | `/api/keranjang` | Kosongkan keranjang |
| `POST` | `/api/pesanan` | Buat pesanan baru |
| `GET` | `/api/pesanan/{id}` | Detail pesanan milik sendiri |
| `GET` | `/api/poin/balance` | Cek saldo poin loyalitas |
| `POST` | `/api/rating` | Submit ulasan & rating |

### 🛡️ Admin (Butuh Token JWT — Role: ADMIN)

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| `GET` | `/api/pesanan` | Daftar semua pesanan (bisa filter) |
| `PATCH` | `/api/pesanan/{id}/status` | Update status pesanan |
| `GET` | `/api/menu` | Kelola menu (termasuk nonaktif) |
| `POST` | `/api/menu` | Tambah menu baru |
| `PUT` | `/api/menu/{id}` | Edit menu |
| `DELETE` | `/api/menu/{id}` | Hapus menu |
| `PATCH` | `/api/menu/{id}/availability` | Toggle ketersediaan menu |
| `GET` | `/api/meja` | Daftar semua meja |
| `POST` | `/api/meja` | Tambah meja & generate QR |
| `DELETE` | `/api/meja/{id}` | Hapus meja (soft delete) |
| `POST` | `/api/promo` | Buat promo baru |
| `PUT` | `/api/promo/{id}` | Edit promo |
| `DELETE` | `/api/promo/{id}` | Hapus promo |
| `GET` | `/api/laporan/stats` | Statistik dashboard harian |
| `GET` | `/api/laporan/trend` | Tren pendapatan |
| `GET` | `/api/laporan/menu-terlaris` | Menu paling laku |
| `GET` | `/api/laporan/export` | Export laporan ke Excel |
| `GET` | `/api/config` | Konfigurasi restoran |
| `PUT` | `/api/config` | Update konfigurasi restoran |

> 📖 **Dokumentasi lengkap interaktif** tersedia di Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 📡 WebSocket Events

Koneksi WebSocket: `ws://localhost:8080/ws` (via SockJS)

| Trigger (REST) | Topic yang Dipublish | Penerima | Payload |
|----------------|---------------------|----------|---------|
| Pesanan baru dibuat | `/topic/admin/pesanan-baru` | Admin dashboard | `PesananBaruWsPayload` |
| Pesanan baru (meja) | `/topic/admin/meja-status` | Admin dashboard | `MejaStatusWsPayload` |
| Status pesanan diupdate | `/topic/pesanan/{pesananId}` | Pelanggan | `PesananStatusWsPayload` |
| Toggle ketersediaan menu | `/topic/menu/availability` | Pelanggan | `MenuAvailabilityWsPayload` |
| Toggle buka/tutup resto | `/topic/resto/status` | Pelanggan | `RestoStatusWsPayload` |
| Pesanan SERVED | `/topic/admin/dashboard-stats` | Admin dashboard | `DashboardStatsWsPayload` |

---

## 🗄️ Database & Migrasi

Database menggunakan **PostgreSQL via Supabase**. Semua perubahan schema dikelola oleh **Flyway** — berjalan otomatis saat server startup.

### File Migrasi (`src/main/resources/db/migration/`)

| File | Isi |
|------|-----|
| `V1__init_schema.sql` | Schema awal semua tabel |
| `V2__seed_data.sql` | Data awal (menu, meja, admin, config) |
| `V4__add_optimistic_locking.sql` | Tambah kolom `version` untuk optimistic lock |
| `V5__fix_admin_password.sql` | Update hash password admin |
| `V6__add_refund_to_poin_tipe.sql` | Tambah tipe REFUND di poin transaksi |
| `V7__seed_client.sql` | Data pelanggan contoh |
| `V8__add_spice_doneness_options.sql` | Opsi level pedas & kematangan menu |
| `V9__allow_guest_rating.sql` | Izinkan guest submit rating |
| `V10__add_usage_to_promo.sql` | Kolom usage count di promo |
| `V11__add_unique_kode_pesanan.sql` | Unique constraint kode pesanan |
| `V12__add_meja_session.sql` | Tabel sesi meja untuk QR token |

### ⚠️ Aturan Migrasi
- **JANGAN edit** file migrasi yang sudah pernah dijalankan
- Untuk perubahan schema → selalu buat **file baru** dengan nomor versi berikutnya
- Format nama: `V{nomor}__{deskripsi_singkat}.sql`

---

## 🔐 Autentikasi & Otorisasi

### JWT Token

| Token | Masa Berlaku | Deskripsi |
|-------|--------------|-----------|
| Access Token | 15 menit | Dikirim di header `Authorization: Bearer <token>` |
| Refresh Token | 7 hari | Dipakai untuk minta access token baru |

### Role

| Role | Akses |
|------|-------|
| `ADMIN` | Semua endpoint dashboard & manajemen |
| `CLIENT` | Endpoint customer (pesanan, keranjang, poin milik sendiri) |
| `PUBLIC` | Endpoint tanpa token (katalog, config, scan QR) |
| Guest | JWT CLIENT dengan flag `isGuest: true` |

### Cara Gunakan di Request

```http
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📦 Perintah yang Tersedia

### Windows
```powershell
# Cara termudah — jalankan dengan load .env otomatis
.\run-backend.ps1

# Jalankan tanpa clean (lebih cepat, skip recompile)
.\mvnw.cmd spring-boot:run

# Build JAR production
.\mvnw.cmd clean package -DskipTests

# Jalankan semua unit test
.\mvnw.cmd clean test

# Compile saja (cek error tanpa run)
.\mvnw.cmd compile
```

### Linux / Mac
```bash
# Jalankan
./mvnw spring-boot:run

# Build JAR production
./mvnw clean package -DskipTests

# Jalankan semua unit test
./mvnw clean test
```

---

## 🧪 Unit Testing

Project memiliki **151 unit test** yang mencakup semua domain, menggunakan JUnit 5 + Mockito + H2 (in-memory DB).

```bash
# Jalankan semua test
.\mvnw.cmd clean test

# Hasil yang diharapkan:
# Tests run: 151, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

### Coverage Test per Domain

| Domain | Controller Test | Service Test |
|--------|----------------|--------------|
| auth | ✅ | ✅ |
| config_resto | ✅ | ✅ |
| keranjang | ✅ | ✅ |
| laporan | ✅ | ✅ |
| meja | ✅ | ✅ |
| menu | ✅ | ✅ |
| pesanan | ✅ | ✅ |
| poin | ✅ | ✅ |
| promo | ✅ | ✅ |
| rating | ✅ | ✅ |
| user | - | ✅ |
| notification | ✅ (WebSocket) | - |

---

## 🤝 Panduan Kontribusi

### 1. Selalu Pull Sebelum Mulai

```bash
git pull origin main
```

### 2. Buat Branch untuk Setiap Fitur/Fix

```bash
git checkout -b feat/nama-fitur
git checkout -b fix/nama-bug
```

### 3. Format Commit

```bash
git commit -m "feat: tambah endpoint GET /api/laporan/trend"
git commit -m "fix: perbaiki NullPointerException di PesananService"
git commit -m "refactor: pisah logika validasi ke helper class"
git commit -m "test: tambah unit test untuk RatingService"
```

### 4. Pastikan Test Lulus Sebelum Push

```bash
.\mvnw.cmd clean test
# BUILD SUCCESS — boleh push
```

### 5. Konvensi Kode Penting

**Layer architecture (wajib diikuti):**
```
Controller → memanggil Service (interface)
ServiceImpl → berisi logika bisnis
Repository → akses database
```

**Wajib pakai `@Transactional` di ServiceImpl:**
```java
@Transactional                    // untuk write operations
@Transactional(readOnly = true)   // untuk read operations
```

**Selalu wrap response dengan `ApiResponse<T>`:**
```java
return ResponseEntity.ok(
    ApiResponse.success("Berhasil", data)
);
```

**Gunakan constructor injection (`@RequiredArgsConstructor`), bukan `@Autowired`:**
```java
@RequiredArgsConstructor
public class MenuServiceImpl {
    private final MenuRepository menuRepository;  // ✅
    
    // ❌ Jangan:
    // @Autowired
    // private MenuRepository menuRepository;
}
```

---

## 🚢 Deployment

Backend di-deploy sebagai **JAR executable** ke server Azure (production).

### Build JAR

```bash
.\mvnw.cmd clean package -DskipTests
# Output: target/aroma-senja-backend-0.0.1-SNAPSHOT.jar
```

### Jalankan JAR di Server

```bash
java -jar target/aroma-senja-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DATABASE_URL=jdbc:postgresql://... \
  --DB_USERNAME=postgres \
  --DB_PASSWORD=... \
  --JWT_SECRET=...
```

### Environment Variables Production

Pastikan semua variabel di `.env.example` sudah di-set di server. Tambahan untuk production:

```bash
# Profil aktif
SPRING_PROFILES_ACTIVE=prod

# CORS — domain frontend production
CORS_ALLOWED_ORIGINS=https://aroma-senja.vercel.app

# QR Code URL — domain production
QR_BASE_URL=https://aroma-senja.vercel.app/customer
```

> Di production, **Swagger UI dinonaktifkan** otomatis (sudah dikonfigurasi di `application.yml` profil `prod`).

---

## 👥 Tim Pengembang

| Nama | GitHub | Role |
|------|--------|------|
| adisonsmn | *(isi username)* | Backend Lead |
| agungsrgh | [@agungsrgh](https://github.com/agungsrgh) | Backend Dev |
| Farhan Hamzah | [@farhan-hamzah](https://github.com/farhan-hamzah) | Backend Dev |
| Nazal Putra | [@NazalDev](https://github.com/NazalDev) | Backend Dev |

---

## 📝 Lisensi

Project ini dibuat untuk keperluan **Tugas Besar mata kuliah Pemrograman Berbasis Objek (PBO)** — Semester 4.

---

<div align="center">
  <sub>Built with ☕ Java & Spring Boot by Tim Aroma Senja</sub>
</div>