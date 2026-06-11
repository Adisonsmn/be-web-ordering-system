package com.aromasenja.domain.meja.dto;

import java.util.UUID;

/**
 * Response DTO untuk data meja.
 * mejaStatus di-derive dari isOccupied di level mapper:
 *   isOccupied=true  → "OCCUPIED" (Terisi)
 *   isOccupied=false → "AVAILABLE" (Kosong)
 * Meja direset ke AVAILABLE hanya melalui aksi manual admin dari dashboard.
 */
public record MejaResponse(
    UUID mejaId,
    Integer nomorMeja,
    String zone,
    boolean isActive,
    boolean isOccupied,
    String qrCodeUrl,
    String mejaStatus  // AVAILABLE atau OCCUPIED
) {}
