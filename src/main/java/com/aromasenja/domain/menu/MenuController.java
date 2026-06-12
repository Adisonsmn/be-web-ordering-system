package com.aromasenja.domain.menu;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.menu.dto.*;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Tag(name = "Menu & Katalog", description = "Endpoint untuk katalog menu dan manajemen item menu")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Ambil daftar semua menu aktif (Public)")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean available) {
        List<MenuResponse> menus = menuService.getAllActiveMenus(category, search, available);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil katalog menu", menus));
    }

    @GetMapping("/populer")
    @Operation(summary = "Ambil menu paling populer (Public) — ditampilkan di halaman selamat datang")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenuPopuler() {
        MenuResponse menu = menuService.getMenuPopuler();
        if (menu == null) {
            return ResponseEntity.ok(ApiResponse.success("Belum ada data menu populer", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil menu populer", menu));
    }

    @GetMapping("/{menuId}")
    @Operation(summary = "Ambil detail lengkap satu item menu (Public)")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> getDetail(@PathVariable UUID menuId) {
        MenuDetailResponse detail = menuService.getMenuDetail(menuId);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil detail menu", detail));
    }

    @GetMapping("/{menuId}/pairings")
    @Operation(summary = "Ambil rekomendasi pairing untuk menu ini (Public)")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getPairings(@PathVariable UUID menuId) {
        List<MenuResponse> pairings = menuService.getPairings(menuId);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil rekomendasi pairing", pairings));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tambah item menu baru (Admin)")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> create(
            @Valid @RequestBody CreateMenuRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MenuDetailResponse response = menuService.createMenu(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Menu berhasil dibuat", response));
    }

    @PutMapping("/{menuId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update seluruh data item menu (Admin)")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> update(
            @PathVariable UUID menuId,
            @Valid @RequestBody UpdateMenuRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MenuDetailResponse response = menuService.updateMenu(menuId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Menu berhasil diperbarui", response));
    }

    @PatchMapping("/{menuId}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle ketersediaan menu (Admin)")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> toggleAvailability(
            @PathVariable UUID menuId,
            @Valid @RequestBody UpdateMenuAvailabilityRequest request) {
        MenuDetailResponse response = menuService.toggleAvailability(menuId, request);
        return ResponseEntity.ok(ApiResponse.success("Status ketersediaan menu berhasil diperbarui", response));
    }

    @PatchMapping("/{menuId}/promo")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set atau hapus promo pada menu (Admin)")
    public ResponseEntity<ApiResponse<MenuDetailResponse>> patchPromo(
            @PathVariable UUID menuId,
            @RequestBody UpdateMenuPromoRequest request) {
        MenuDetailResponse response = menuService.patchMenuPromo(menuId, request);
        return ResponseEntity.ok(ApiResponse.success("Promo menu berhasil diperbarui", response));
    }

    @DeleteMapping("/{menuId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Hapus item menu - Soft Delete (Admin)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID menuId) {
        menuService.softDeleteMenu(menuId);
        return ResponseEntity.ok(ApiResponse.success("Menu berhasil dihapus (soft delete)", null));
    }
}
