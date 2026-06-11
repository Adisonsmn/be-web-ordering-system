package com.aromasenja.domain.user;

import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.user.dto.UpdateProfileRequest;
import com.aromasenja.domain.user.dto.UserProfileResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

/**
 * Service interface untuk manajemen profil pengguna.
 * Extends UserDetailsService agar UserServiceImpl bisa digunakan oleh
 * DaoAuthenticationProvider (Spring Security) untuk verifikasi password saat login.
 */
public interface UserService extends UserDetailsService {

    /**
     * Ambil profil pengguna yang sedang login.
     *
     * @param currentUser principal dari JWT token
     * @return data profil tanpa password
     */
    UserProfileResponse getProfile(UserPrincipal currentUser);

    /**
     * Update profil pengguna.
     * Service memvalidasi bahwa user hanya bisa update profilnya sendiri
     * (kecuali ADMIN yang bisa update siapapun).
     *
     * @param userId      UUID user yang ingin diupdate
     * @param request     data update
     * @param currentUser principal dari JWT token
     * @return profil yang sudah diupdate
     */
    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request, UserPrincipal currentUser);

    /**
     * Mengubah kata sandi pengguna yang sedang login.
     *
     * @param currentUser principal dari JWT token
     * @param request     data password lama dan baru
     */
    void changePassword(UserPrincipal currentUser, com.aromasenja.domain.auth.dto.ChangePasswordRequest request);
}

