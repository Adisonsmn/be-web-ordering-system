package com.aromasenja.domain.rating.dto;

import java.util.List;

public record MenuRatingResponse(
    double avgRating,
    List<RatingResponse> ratings
) {}
