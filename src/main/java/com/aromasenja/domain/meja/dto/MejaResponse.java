package com.aromasenja.domain.meja.dto;

import java.util.UUID;

public record MejaResponse(
    UUID mejaId,
    Integer nomorMeja,
    String zone,
    boolean isActive,
    boolean isOccupied,
    String qrCodeUrl
) {}
