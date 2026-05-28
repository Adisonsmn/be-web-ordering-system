package com.aromasenja.domain.meja;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.domain.meja.dto.CreateMejaRequest;
import com.aromasenja.domain.meja.dto.MejaResponse;
import com.aromasenja.domain.meja.dto.ScanMejaResponse;
import com.aromasenja.domain.meja.dto.UpdateMejaStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meja")
@RequiredArgsConstructor
@Tag(name = "Meja & QR Code", description = "Endpoints untuk manajemen meja, validasi scan QR code, dan real-time status")
public class MejaController {

    private final MejaService mejaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil semua daftar meja aktif", description = "Hanya dapat diakses oleh ADMIN.")
    public ResponseEntity<ApiResponse<List<MejaResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Daftar meja aktif berhasil diambil", mejaService.getAllMeja())
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Buat meja baru", description = "Menambahkan meja baru ke sistem. Hanya dapat diakses oleh ADMIN.")
    public ResponseEntity<ApiResponse<MejaResponse>> create(
            @Valid @RequestBody CreateMejaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Meja baru berhasil dibuat", mejaService.createMeja(request))
        );
    }

    @DeleteMapping("/{mejaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft delete meja", description = "Menonaktifkan meja (soft delete is_active = false). Hanya dapat diakses oleh ADMIN.")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable UUID mejaId) {
        mejaService.softDeleteMeja(mejaId);
        return ResponseEntity.ok(
                ApiResponse.success("Meja berhasil dihapus")
        );
    }

    @GetMapping("/{mejaId}/qr")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Download QR Code meja", description = "Mendapatkan QR Code meja dalam format PNG. Hanya dapat diakses oleh ADMIN.")
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable UUID mejaId) {
        byte[] qrBytes = mejaService.generateQrCode(mejaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(qrBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/scan/{mejaId}")
    @Operation(summary = "Scan QR Code meja (Public entrypoint)", description = "Memvalidasi scan meja dan mengembalikan informasi detail meja beserta status operasional restoran.")
    public ResponseEntity<ApiResponse<ScanMejaResponse>> scan(
            @PathVariable UUID mejaId) {
        return ResponseEntity.ok(
                ApiResponse.success("Scan meja berhasil", mejaService.scanQr(mejaId))
        );
    }

    @PatchMapping("/{mejaId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update status okupansi meja", description = "Mengubah status isOccupied meja dan memicu update real-time ke admin dashboard. Hanya dapat diakses oleh ADMIN.")
    public ResponseEntity<ApiResponse<MejaResponse>> updateStatus(
            @PathVariable UUID mejaId,
            @Valid @RequestBody UpdateMejaStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Status okupansi meja berhasil diperbarui", mejaService.updateStatus(mejaId, request))
        );
    }
}
