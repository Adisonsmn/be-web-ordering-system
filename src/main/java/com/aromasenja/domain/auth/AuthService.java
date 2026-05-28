package com.aromasenja.domain.auth;

import com.aromasenja.domain.auth.dto.*;

/**
 * Service interface untuk autentikasi dan manajemen sesi.
 */
public interface AuthService {

    /** Login dengan email dan password. Return JWT pair + profil. */
    LoginResponse login(LoginRequest request);

    /** Registrasi member baru. Otomatis buat User + Client. Return JWT pair + profil. */
    LoginResponse register(RegisterRequest request);

    /**
     * Issue access token baru menggunakan refresh token yang valid.
     * Refresh token lama TIDAK di-revoke (masih bisa digunakan sampai expired).
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * Login sebagai guest via QR Code meja.
     * Tidak ada record DB — hanya return access token dengan klaim isGuest=true.
     * Guest tidak mendapat refresh token.
     */
    LoginResponse loginAsGuest(GuestLoginRequest request);

    /**
     * Logout — revoke refresh token di DB.
     * Access token tetap valid sampai expired (stateless), tapi sangat singkat (15 menit).
     */
    void logout(String refreshToken);
}
