package com.aromasenja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point aplikasi Aroma Senja Backend.
 * QR-based restaurant self-ordering system.
 *
 * @EnableJpaAuditing didefinisikan di JpaAuditingConfig untuk memisahkan concerns.
 * @EnableScheduling diaktifkan untuk auto-close restoran ketika jam tutup sudah lewat.
 */
@SpringBootApplication
@EnableScheduling
public class AromaSenjaApplication {

    @jakarta.annotation.PostConstruct
    public void init() {
        // Enforce timezone agar konsisten dengan perhitungan laporan di LaporanServiceImpl
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Jakarta"));
    }

    public static void main(String[] args) {
        SpringApplication.run(AromaSenjaApplication.class, args);
    }
}
