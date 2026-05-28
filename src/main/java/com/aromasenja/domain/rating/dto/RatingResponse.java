package com.aromasenja.domain.rating.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RatingResponse(
    UUID ratingId,
    UUID clientId,
    String clientName,
    UUID menuId,
    UUID pesananId,
    Short bintang,
    String ulasan,
    boolean isOverall,
    boolean isPublic,
    LocalDateTime createdAt
) {}
