package com.aromasenja.domain.menu.dto;

import java.util.UUID;

public record MenuAvailabilityWsPayload(
    UUID menuId,
    String menuName,
    boolean isAvailable
) {}
