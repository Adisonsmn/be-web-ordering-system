package com.aromasenja.domain.rating;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.rating.dto.CreateRatingRequest;
import com.aromasenja.domain.rating.dto.MenuRatingResponse;
import com.aromasenja.domain.rating.dto.PesananRatingStatusResponse;
import com.aromasenja.domain.rating.dto.RatingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rating")
@RequiredArgsConstructor
@Tag(name = "Rating & Ulasan", description = "Endpoint untuk memberikan ulasan dan rating makanan")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit rating & ulasan ulasan baru (Member Only)")
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @Valid @RequestBody CreateRatingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        RatingResponse response = ratingService.submitRating(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rating berhasil dikirim", response));
    }

    @GetMapping("/menu/{menuId}")
    @Operation(summary = "Ambil daftar ulasan publik untuk satu item menu (Public)")
    public ResponseEntity<ApiResponse<MenuRatingResponse>> getRatingsByMenu(@PathVariable UUID menuId) {
        MenuRatingResponse response = ratingService.getPublicRatingsByMenu(menuId);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil ulasan menu", response));
    }

    @GetMapping("/pesanan/{pesananId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cek apakah pesanan tertentu sudah dirating (Member / Guest / Admin)")
    public ResponseEntity<ApiResponse<PesananRatingStatusResponse>> checkRatingStatus(
            @PathVariable UUID pesananId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PesananRatingStatusResponse response = ratingService.checkRatingStatus(pesananId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengecek status rating pesanan", response));
    }
}
