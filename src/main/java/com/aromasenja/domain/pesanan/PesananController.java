package com.aromasenja.domain.pesanan;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.pesanan.dto.*;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/pesanan")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pesanan & Pembayaran", description = "Endpoint untuk pemesanan mandiri dan pengelolaan order")
public class PesananController {

    private final PesananService pesananService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Buat pesanan baru dari keranjang (Guest / Member)")
    public ResponseEntity<ApiResponse<PesananResponse>> create(
            @Valid @RequestBody CreatePesananRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PesananResponse response = pesananService.createPesanan(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pesanan berhasil dibuat", response));
    }

    @GetMapping("/{pesananId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil detail pesanan lengkap (Guest / Member / Admin)")
    public ResponseEntity<ApiResponse<PesananResponse>> getDetail(
            @PathVariable UUID pesananId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PesananResponse response = pesananService.getPesananDetail(pesananId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil detail pesanan", response));
    }

    @GetMapping("/{pesananId}/struk")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil data struk digital pesanan (Guest / Member)")
    public ResponseEntity<ApiResponse<StrukPesananResponse>> getStruk(
            @PathVariable UUID pesananId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StrukPesananResponse response = pesananService.getStruk(pesananId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil struk pesanan", response));
    }

    @GetMapping("/riwayat")
    @PreAuthorize("hasRole('CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil riwayat pesanan milik client login (Member)")
    public ResponseEntity<ApiResponse<Page<PesananResponse>>> getRiwayat(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PesananResponse> response = pesananService.getRiwayatPesanan(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil riwayat pesanan", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil daftar semua pesanan dengan filter (Admin)")
    public ResponseEntity<ApiResponse<Page<PesananResponse>>> getAllAdmin(
            @RequestParam(required = false) StatusPesanan status,
            @RequestParam(required = false) UUID mejaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10) Pageable pageable) {
        java.time.LocalDateTime startLdt = null;
        java.time.LocalDateTime endLdt = null;
        try {
            if (startDate != null && !startDate.isBlank()) {
                startLdt = OffsetDateTime.parse(startDate).toLocalDateTime();
            }
            if (endDate != null && !endDate.isBlank()) {
                endLdt = OffsetDateTime.parse(endDate).toLocalDateTime();
            }
        } catch (Exception e) {
            log.warn("Gagal parse startDate/endDate: startDate={}, endDate={}", startDate, endDate, e);
        }
        Page<PesananResponse> response = pesananService.getAllPesananAdmin(status, mejaId, tanggal, startLdt, endLdt, category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil daftar pesanan", response));
    }

    @GetMapping("/kanban")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil data kanban board (NEW, PREPARING, READY) masing-masing max 20 item (Admin)")
    public ResponseEntity<ApiResponse<KanbanPesananResponse>> getKanban() {
        KanbanPesananResponse response = pesananService.getKanbanPesananAdmin();
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil data kanban pesanan", response));
    }

    @PatchMapping("/{pesananId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update status tahapan pesanan (Admin)")
    public ResponseEntity<ApiResponse<PesananResponse>> updateStatus(
            @PathVariable UUID pesananId,
            @Valid @RequestBody UpdateStatusPesananRequest request) {
        PesananResponse response = pesananService.updateStatus(pesananId, request);
        return ResponseEntity.ok(ApiResponse.success("Status pesanan berhasil diperbarui", response));
    }

    @PatchMapping("/{pesananId}/bayar")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Catat pembayaran dan selesaikan pesanan (Admin)")
    public ResponseEntity<ApiResponse<PesananResponse>> bayar(
            @PathVariable UUID pesananId,
            @Valid @RequestBody BayarPesananRequest request) {
        PesananResponse response = pesananService.bayarPesanan(pesananId, request);
        return ResponseEntity.ok(ApiResponse.success("Pembayaran pesanan berhasil dicatat", response));
    }

    @PatchMapping("/{pesananId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Batalkan pesanan (Admin)")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable UUID pesananId) {
        pesananService.cancelPesanan(pesananId);
        return ResponseEntity.ok(ApiResponse.success("Pesanan berhasil dibatalkan", null));
    }
}
