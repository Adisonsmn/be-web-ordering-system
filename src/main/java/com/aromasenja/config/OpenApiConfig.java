package com.aromasenja.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasi OpenAPI / Swagger UI via Springdoc.
 *
 * Akses Swagger UI: http://localhost:8080/swagger-ui.html
 * Akses API Docs JSON: http://localhost:8080/api-docs
 *
 * Untuk endpoint yang butuh JWT, tambahkan:
 * @SecurityRequirement(name = "bearerAuth") di controller/endpoint.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aroma Senja API")
                        .version("1.0.0")
                        .description("""
                                REST API untuk sistem pemesanan mandiri berbasis QR Code.
                                
                                ### 🔐 Autentikasi
                                Gunakan endpoint `POST /api/auth/login` untuk mendapatkan access token,
                                lalu klik tombol **'Authorize'** dan masukkan token dengan format: `Bearer {token}`
                                
                                ---
                                
                                ### 📡 WebSocket (STOMP) API
                                Sistem ini menggunakan WebSocket STOMP untuk *real-time updates*. 
                                Endpoint koneksi WebSocket: `ws://{host}:{port}/ws`
                                
                                **A. Subscribe Topics — Pelanggan (Client)**
                                1. **`/topic/pesanan/{pesananId}`**
                                   - **Fungsi**: Menerima update status pesanan spesifik secara real-time.
                                   - **Payload**: `PesananStatusWsPayload` (status: PREPARING, READY, SERVED, CANCELLED).
                                2. **`/topic/menu/availability`**
                                   - **Fungsi**: Menerima update ketersediaan stok menu (habis/tersedia).
                                   - **Payload**: `MenuAvailabilityWsPayload`.
                                3. **`/topic/resto/status`**
                                   - **Fungsi**: Menerima update status operasional restoran (buka/tutup).
                                   - **Payload**: `RestoStatusWsPayload`.
                                
                                **B. Subscribe Topics — Admin Dashboard**
                                1. **`/topic/admin/pesanan-baru`**
                                   - **Fungsi**: Notifikasi pesanan baru masuk untuk dapur/kasir.
                                   - **Payload**: `PesananBaruWsPayload`.
                                2. **`/topic/admin/meja-status`**
                                   - **Fungsi**: Update real-time status okupansi meja (terisi/kosong).
                                   - **Payload**: `MejaStatusWsPayload`.
                                """)
                        .contact(new Contact()
                                .name("Aroma Senja Dev Team")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name("bearerAuth")
                                        .description("Masukkan JWT access token dari endpoint /api/auth/login")));
    }
}
