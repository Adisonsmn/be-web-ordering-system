package com.aromasenja.notification.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload WebSocket untuk event pesanan baru.
 * Di-publish ke: /topic/admin/pesanan-baru
 * Konsumen: Admin dashboard
 */
public record PesananBaruWsPayload(
        UUID pesananId,
        String kodePesanan,    // Human-readable: "AR-2048"
        Integer nomorMeja,
        String zone,           // "Indoor" atau "Outdoor"
        BigDecimal total,
        Integer jumlahItem,
        LocalDateTime createdAt
) {}
