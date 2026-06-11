package com.aromasenja.domain.meja.dto;

import java.util.UUID;

public record ScanMejaResponse(
    UUID mejaId,
    Integer nomorMeja,
    String zone,
    boolean isActive,
    boolean isOccupied,
    boolean isOpen,
    String sessionToken
) {
    public ScanMejaResponse(UUID mejaId, Integer nomorMeja, String zone, boolean isActive, boolean isOccupied, boolean isOpen) {
        this(mejaId, nomorMeja, zone, isActive, isOccupied, isOpen, null);
    }
}
