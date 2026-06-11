package com.aromasenja.notification.payload;

import java.util.UUID;

/**
 * Payload WebSocket untuk update status meja (terisi / kosong).
 * Di-publish ke: /topic/admin/meja-status
 * Konsumen: Admin dashboard (kanban meja)
 */
public record MejaStatusWsPayload(
        UUID mejaId,
        Integer nomorMeja,
        boolean isOccupied,   // true = sedang terisi oleh tamu
        String status         // AVAILABLE, OCCUPIED
) {
    public MejaStatusWsPayload(UUID mejaId, Integer nomorMeja, boolean isOccupied) {
        this(mejaId, nomorMeja, isOccupied, isOccupied ? "OCCUPIED" : "AVAILABLE");
    }
}
