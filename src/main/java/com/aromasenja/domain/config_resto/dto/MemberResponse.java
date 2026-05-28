package com.aromasenja.domain.config_resto.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResponse(
    UUID id,
    UUID clientId,
    String name,
    String email,
    String phone,
    Integer totalPoint,
    LocalDateTime tanggalDaftar,
    LocalDateTime lastOrder,
    Boolean isActive
) {}
