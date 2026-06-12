package com.aromasenja.domain.config_resto;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.dto.UpdateRestoConfigRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "Konfigurasi Restoran", description = "Endpoints untuk mengambil dan mengatur status operasional serta profil restoran")
public class RestoConfigController {

    private final RestoConfigService restoConfigService;

    @GetMapping
    @Operation(summary = "Ambil konfigurasi & status restoran", description = "Diakses publik tanpa token. Digunakan client saat scan QR.")
    public ResponseEntity<ApiResponse<RestoConfigResponse>> getConfig() {
        return ResponseEntity.ok(
                ApiResponse.success("Konfigurasi restoran berhasil diambil", restoConfigService.getConfig())
        );
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update konfigurasi restoran", description = "Hanya dapat diakses oleh ADMIN. Mengubah jam buka/tutup, status buka, nama, dll.")
    public ResponseEntity<ApiResponse<RestoConfigResponse>> updateConfig(
            @Valid @RequestBody UpdateRestoConfigRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Konfigurasi restoran berhasil diperbarui", restoConfigService.updateConfig(request))
        );
    }
}
