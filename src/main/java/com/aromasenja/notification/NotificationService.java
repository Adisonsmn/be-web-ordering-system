package com.aromasenja.notification;

import com.aromasenja.common.websocket.WebSocketEventPublisher;
import com.aromasenja.notification.payload.MejaStatusWsPayload;
import com.aromasenja.notification.payload.PesananBaruWsPayload;
import com.aromasenja.notification.payload.PesananStatusWsPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service untuk publish semua WebSocket event ke topic yang sesuai.
 *
 * ATURAN:
 * - Domain services HARUS inject NotificationService ini, bukan WebSocketEventPublisher langsung
 * - Setiap publish di-wrap try-catch — WebSocket error TIDAK boleh membatalkan REST response
 * - Log error jika publish gagal, tapi jangan rethrow
 *
 * Topic yang dikelola:
 * - /topic/admin/pesanan-baru     → pesanan baru untuk admin dashboard
 * - /topic/admin/meja-status      → status meja berubah untuk admin dashboard
 * - /topic/pesanan/{pesananId}    → update status pesanan untuk client
 * - /topic/menu/availability      → ketersediaan menu berubah untuk semua client
 * - /topic/resto/status           → status buka/tutup restoran untuk semua client
 * - /topic/admin/dashboard-stats  → statistik dashboard untuk admin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WebSocketEventPublisher publisher;

    // ── Admin topics ─────────────────────────────────────────────────────────

    /** Notifikasi pesanan baru masuk ke dashboard admin. */
    public void publishPesananBaru(PesananBaruWsPayload payload) {
        try {
            publisher.publish("/topic/admin/pesanan-baru", payload);
        } catch (Exception e) {
            log.error("Gagal publish WebSocket event pesanan-baru: pesananId={}",
                    payload.pesananId(), e);
        }
    }

    /** Notifikasi perubahan status meja (terisi / kosong) ke dashboard admin. */
    public void publishMejaStatus(MejaStatusWsPayload payload) {
        try {
            publisher.publish("/topic/admin/meja-status", payload);
        } catch (Exception e) {
            log.error("Gagal publish WebSocket event meja-status: mejaId={}",
                    payload.mejaId(), e);
        }
    }

    /**
     * Notifikasi update statistik real-time ke admin dashboard.
     *
     * @param payload object apapun yang berisi data statistik
     */
    public void publishDashboardStats(Object payload) {
        try {
            publisher.publish("/topic/admin/dashboard-stats", payload);
        } catch (Exception e) {
            log.error("Gagal publish WebSocket event dashboard-stats", e);
        }
    }

    // ── Client topics ────────────────────────────────────────────────────────

    /**
     * Notifikasi perubahan status pesanan ke client yang menunggu.
     *
     * @param pesananId UUID pesanan yang statusnya berubah
     * @param payload   detail status terbaru
     */
    public void publishStatusPesanan(UUID pesananId, PesananStatusWsPayload payload) {
        try {
            publisher.publish("/topic/pesanan/" + pesananId, payload);
        } catch (Exception e) {
            log.error("Gagal publish WebSocket event status-pesanan: pesananId={}", pesananId, e);
        }
    }

    /**
     * Notifikasi perubahan ketersediaan menu (is_available toggle).
     * Di-publish saat admin mengubah availability menu — client yang sedang di halaman menu
     * akan mendapat update real-time.
     *
     * @param payload object berisi menuId dan isAvailable
     */
    public void publishMenuAvailability(Object payload) {
        try {
            publisher.publish("/topic/menu/availability", payload);
        } catch (Exception e) {
            log.error("Gagal publish WebSocket event menu-availability", e);
        }
    }

    /**
     * Notifikasi perubahan status buka/tutup restoran.
     * Di-publish saat admin toggle is_open di config_resto.
     *
     * @param payload object berisi isOpen boolean
     */
    public void publishRestoStatus(Object payload) {
        try {
            publisher.publish("/topic/resto/status", payload);
        } catch (Exception e) {
            log.error("Gagal publish WebSocket event resto-status", e);
        }
    }
}
