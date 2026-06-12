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

        LocalTime now = LocalTime.now();
        LocalTime closeTime = config.getCloseTime();

        if (closeTime == null) return;

        // Tutup jika jam sekarang sudah melewati closeTime
        if (now.isAfter(closeTime)) {
            config.setOpen(false);
            restoConfigRepository.save(config);

            log.info("Restoran otomatis ditutup karena jam tutup ({}) sudah terlewati. Jam sekarang: {}",
                    closeTime, now);

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
