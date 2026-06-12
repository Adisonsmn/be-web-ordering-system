package com.aromasenja.domain.keranjang;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.keranjang.dto.*;
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
@RequestMapping("/api/keranjang")
@RequiredArgsConstructor
@Tag(name = "Keranjang Belanja", description = "Endpoint untuk manajemen keranjang belanja guest dan client")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')") // Mendukung guest (ROLE_CLIENT) & member (ROLE_CLIENT) & fallback admin
public class KeranjangController {

    private final KeranjangService keranjangService;

    @GetMapping
    @Operation(summary = "Ambil isi keranjang belanja aktif")
    public ResponseEntity<ApiResponse<KeranjangResponse>> getCart(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KeranjangResponse response = keranjangService.getKeranjang(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil keranjang belanja", response));
    }

    @PostMapping("/items")
    @Operation(summary = "Tambah item menu ke keranjang")
    public ResponseEntity<ApiResponse<KeranjangResponse>> addItem(
            @Valid @RequestBody AddKeranjangItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KeranjangResponse response = keranjangService.addItem(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Item berhasil ditambahkan ke keranjang", response));
    }

    @PutMapping("/items/{detailId}")
    @Operation(summary = "Update quantity atau catatan item di keranjang")
    public ResponseEntity<ApiResponse<KeranjangResponse>> updateItem(
            @PathVariable UUID detailId,
            @Valid @RequestBody UpdateKeranjangItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KeranjangResponse response = keranjangService.updateItem(detailId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Item keranjang berhasil diperbarui", response));
    }

    @DeleteMapping("/items/{detailId}")
    @Operation(summary = "Hapus satu item dari keranjang")
    public ResponseEntity<ApiResponse<KeranjangResponse>> removeItem(
            @PathVariable UUID detailId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        KeranjangResponse response = keranjangService.removeItem(detailId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Item keranjang berhasil dihapus", response));
    }

    @DeleteMapping
    @Operation(summary = "Kosongkan seluruh isi keranjang belanja")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        keranjangService.clearKeranjang(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Keranjang belanja berhasil dikosongkan", null));
    }
}
