package com.aromasenja.notification.payload;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload WebSocket untuk update status pesanan.
 * Di-publish ke: /topic/pesanan/{pesananId}
 * Konsumen: Client yang menunggu statusnya
 */
public record PesananStatusWsPayload(
        UUID pesananId,
        String status,          // "new" | "preparing" | "ready" | "served" | "cancelled"
        Integer estimasiMenit,  // null jika tidak relevan (misal saat CANCELLED atau SERVED)
        LocalDateTime updatedAt
) {}
