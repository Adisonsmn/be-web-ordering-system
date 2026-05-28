package com.aromasenja.notification.payload;

import java.time.LocalDateTime;

public record RestoStatusWsPayload(
    boolean isOpen,
    String pesan,
    LocalDateTime timestamp
) {}
