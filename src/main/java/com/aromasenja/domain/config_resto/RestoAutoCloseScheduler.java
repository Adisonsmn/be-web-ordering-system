package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.notification.NotificationService;
import com.aromasenja.notification.payload.RestoStatusWsPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Scheduler untuk auto-close restoran ketika jam tutup operasional sudah terlewati.
 * Berjalan setiap menit untuk mengecek apakah waktu saat ini sudah melewati closeTime.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestoAutoCloseScheduler {

    private final RestoConfigRepository restoConfigRepository;
    private final NotificationService notificationService;

    /**
     * Setiap menit, cek apakah restoran perlu ditutup otomatis.
     * Auto-close terjadi jika:
     * 1. Restoran sedang buka (isOpen = true)
     * 2. Waktu saat ini sudah melewati closeTime
     */
    @Scheduled(cron = "0 * * * * *") // Setiap awal menit
    @Transactional
    public void autoCloseIfNeeded() {
        Optional<RestoConfig> configOpt = restoConfigRepository.findFirstBy();
        if (configOpt.isEmpty()) return;

        RestoConfig config = configOpt.get();

        // Hanya tutup jika sekarang sedang buka
        if (!config.isOpen()) return;

        // Gunakan timezone WIB (Asia/Jakarta) secara eksplisit
        // agar konsisten dengan waktu yang diinput user di frontend,
        // terlepas dari timezone server (Azure VM default: UTC)
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Jakarta"));
        LocalTime closeTime = config.getCloseTime();
        LocalTime openTime = config.getOpenTime();

        if (closeTime == null) return;

        // Deteksi skenario cross-midnight: openTime > closeTime
        // Contoh: open=23:00, close=07:00 → restoran buka melewati tengah malam
        boolean isCrossMidnight = openTime != null && openTime.isAfter(closeTime);

        boolean shouldClose;
        if (isCrossMidnight) {
            // Cross-midnight: restoran BUKA antara openTime s/d tengah malam DAN tengah malam s/d closeTime
            // Harus TUTUP jika now sudah lewat closeTime DAN belum mencapai openTime
            // Contoh: open=23:00, close=07:00 → tutup jika now berada di antara 07:00–22:59
            shouldClose = now.isAfter(closeTime) && now.isBefore(openTime);
        } else {
            // Normal (tidak cross-midnight): tutup jika now sudah lewat closeTime
            shouldClose = now.isAfter(closeTime);
        }

        if (shouldClose) {
            config.setOpen(false);
            restoConfigRepository.save(config);

            log.info("Restoran otomatis ditutup karena jam tutup ({}) sudah terlewati. Jam sekarang: {} [cross-midnight={}]",
                    closeTime, now, isCrossMidnight);

            // Broadcast WebSocket ke customer agar redirect ke splash "Tutup"
            try {
                RestoStatusWsPayload payload = new RestoStatusWsPayload(
                        false,
                        "Restoran telah tutup sesuai jam operasional.",
                        LocalDateTime.now()
                );
                notificationService.publishRestoStatus(payload);
            } catch (Exception e) {
                log.error("Gagal broadcast WebSocket auto-close: {}", e.getMessage());
            }
        }
    }
}
