package com.aromasenja.domain.auth;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.auth.dto.*;
import com.aromasenja.domain.user.UserService;
import com.aromasenja.domain.user.dto.UpdateProfileRequest;
import com.aromasenja.domain.user.dto.UserProfileResponse;
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

/**
 * Controller untuk autentikasi, manajemen sesi, dan profil pengguna aktif.
 * Endpoint register/login/refresh/guest adalah PUBLIC.
 * Endpoint /me membutuhkan JWT.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autentikasi, registrasi, manajemen token, dan profil pengguna")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // ── Endpoint PUBLIC ───────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Registrasi member baru", description = "Buat akun baru dengan email dan password. Return JWT pair.")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Registrasi berhasil", authService.register(request))
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Login dengan email dan password", description = "Return access token + refresh token + profil user.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Login berhasil", authService.login(request))
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Gunakan refresh token untuk mendapatkan access token baru.")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Token berhasil diperbarui", authService.refreshToken(request))
        );
    }

    @PostMapping("/guest")
    @Operation(
            summary = "Login sebagai guest via QR Code",
            description = "Scan QR meja → ekstrak tableId → kirim ke endpoint ini. Return access token tanpa profil DB."
    )
    public ResponseEntity<ApiResponse<LoginResponse>> loginAsGuest(
            @Valid @RequestBody GuestLoginRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Login guest berhasil", authService.loginAsGuest(request))
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout — revoke refresh token", description = "Kirim refresh token di request body untuk diinvalidasi. Access token tetap valid sampai expired (15 menit).")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout berhasil"));
    }

    // ── Endpoint yang membutuhkan JWT ─────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil profil pengguna yang sedang login",
            description = "Membaca data user dari JWT. Return profil lengkap termasuk total poin jika CLIENT.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(
                ApiResponse.success("Profil berhasil diambil", userService.getProfile(currentUser))
        );
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update profil pengguna yang sedang login",
            description = "Memperbarui name, phone, dan avatar_url. Email tidak bisa diubah setelah registrasi.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(
                ApiResponse.success("Profil berhasil diperbarui",
                        userService.updateProfile(currentUser.getUserId(), request, currentUser))
        );
    }
}
