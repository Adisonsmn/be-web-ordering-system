package com.aromasenja.domain.poin;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.poin.dto.PoinBalanceResponse;
import com.aromasenja.domain.poin.dto.PoinKalkulasiRequest;
import com.aromasenja.domain.poin.dto.PoinKalkulasiResponse;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/poin")
@RequiredArgsConstructor
@Tag(name = "Poin Loyalitas", description = "Endpoint untuk cek saldo poin, riwayat poin, dan kalkulasi diskon poin")
public class PoinController {

    private final PoinService poinService;

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil saldo poin aktif dan info konversi poin (Member Only)")
    public ResponseEntity<ApiResponse<PoinBalanceResponse>> getBalance(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PoinBalanceResponse response = poinService.getPoinBalance(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil saldo poin", response));
    }

    @GetMapping("/riwayat")
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil riwayat transaksi poin terurut dari terbaru (Member Only)")
    public ResponseEntity<ApiResponse<Page<PoinRiwayatResponse>>> getRiwayat(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PoinRiwayatResponse> response = poinService.getRiwayatPoin(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil riwayat poin", response));
    }

    @PostMapping("/kalkulasi")
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Hitung preview potongan harga menggunakan poin (Member Only)")
    public ResponseEntity<ApiResponse<PoinKalkulasiResponse>> kalkulasi(
            @Valid @RequestBody PoinKalkulasiRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PoinKalkulasiResponse response = poinService.kalkulasiPoin(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Berhasil menghitung kalkulasi poin", response));
    }

    @GetMapping("/estimasi")
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Hitung estimasi poin yang akan didapatkan dari subtotal belanja (Member Only)")
    public ResponseEntity<ApiResponse<com.aromasenja.domain.poin.dto.PoinEstimasiResponse>> getEstimasi(
            @RequestParam("subtotal") java.math.BigDecimal subtotal,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        com.aromasenja.domain.poin.dto.PoinEstimasiResponse response = poinService.getEstimasiPoin(subtotal, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Berhasil menghitung estimasi poin", response));
    }
}
