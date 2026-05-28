package com.aromasenja.domain.rating;

import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.rating.dto.CreateRatingRequest;
import com.aromasenja.domain.rating.dto.MenuRatingResponse;
import com.aromasenja.domain.rating.dto.PesananRatingStatusResponse;
import com.aromasenja.domain.rating.dto.RatingResponse;

import java.util.UUID;

public interface RatingService {

    RatingResponse submitRating(CreateRatingRequest request, UserPrincipal currentUser);

    MenuRatingResponse getPublicRatingsByMenu(UUID menuId);

    PesananRatingStatusResponse checkRatingStatus(UUID pesananId, UserPrincipal currentUser);
}
