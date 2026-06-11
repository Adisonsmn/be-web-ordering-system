package com.aromasenja.domain.promo;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.domain.promo.dto.CreatePromoRequest;
import com.aromasenja.domain.promo.dto.PromoResponse;
import com.aromasenja.domain.promo.dto.PromoHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promo")
@RequiredArgsConstructor
@Tag(name = "Promo & Diskon", description = "Endpoint untuk katalog promo dan manajemen kampanye diskon")
public class PromoController {

    private final PromoService promoService;

    @GetMapping
    @Operation(summary = "Ambil daftar semua promo aktif (Public)")
    public ResponseEntity<ApiResponse<List<PromoResponse>>> getActivePromos() {
        List<PromoResponse> promos = promoService.getActivePromosForClient();
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil daftar promo aktif", promos));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil daftar semua promo dengan filter status (Admin)")
    public ResponseEntity<ApiResponse<List<PromoResponse>>> getAllPromosForAdmin(
            @RequestParam(required = false) String status) {
        List<PromoResponse> promos = promoService.getAllPromosForAdmin(status);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil daftar promo admin", promos));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Buat promo baru (Admin)")
    public ResponseEntity<ApiResponse<PromoResponse>> create(
            @Valid @RequestBody CreatePromoRequest request) {
        PromoResponse response = promoService.createPromo(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Promo berhasil dibuat", response));
    }

    @PutMapping("/{promoId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update data promo (Admin)")
    public ResponseEntity<ApiResponse<PromoResponse>> update(
            @PathVariable UUID promoId,
            @Valid @RequestBody CreatePromoRequest request) {
        PromoResponse response = promoService.updatePromo(promoId, request);
        return ResponseEntity.ok(ApiResponse.success("Promo berhasil diperbarui", response));
    }

    @DeleteMapping("/{promoId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Nonaktifkan / Hapus promo - Soft Delete (Admin)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID promoId) {
        promoService.deletePromo(promoId);
        return ResponseEntity.ok(ApiResponse.success("Promo berhasil dinonaktifkan (soft delete)", null));
    }

    @GetMapping("/{promoId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil riwayat penggunaan promo (Admin Only)")
    public ResponseEntity<ApiResponse<Page<PromoHistoryResponse>>> getPromoHistory(
            @PathVariable UUID promoId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PromoHistoryResponse> history = promoService.getPromoHistory(promoId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil riwayat penggunaan promo", history));
    }
}
