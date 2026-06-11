package com.aromasenja.domain.user;

import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.auth.dto.ChangePasswordRequest;
import com.aromasenja.domain.user.dto.UpdateProfileRequest;
import com.aromasenja.domain.user.dto.UserProfileResponse;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementasi UserService dan UserDetailsService.
 *
 * loadUserByUsername dipanggil oleh Spring Security (DaoAuthenticationProvider)
 * saat login — ini yang memuat password hash dari DB untuk verifikasi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;


    // ── UserDetailsService ───────────────────────────────────────────────────

    /**
     * Load user dari DB berdasarkan email untuk verifikasi password saat login.
     * Dipanggil oleh DaoAuthenticationProvider — BUKAN oleh JwtAuthFilter.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User tidak ditemukan dengan email: " + email));
        return UserPrincipal.from(user);
    }

    // ── UserService ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        Client client = null;
        if (currentUser.hasRole("CLIENT")) {
            client = clientRepository.findByUser_Id(currentUser.getUserId()).orElse(null);
        }

        return mapToResponse(user, client);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request,
                                             UserPrincipal currentUser) {
        // Validasi kepemilikan: user hanya bisa update profilnya sendiri, kecuali admin
        if (!currentUser.hasRole("ADMIN") && !currentUser.getUserId().equals(userId)) {
            throw new UnauthorizedException("Kamu tidak berhak mengubah profil user lain");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        user.setName(request.name().trim());
        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }
        // avatarUrl boleh null — set null jika user ingin hapus avatar
        user.setAvatarUrl(request.avatarUrl());

        user = userRepository.save(user);

        Client client = null;
        if (user.getRole().name().equals("CLIENT")) {
            client = clientRepository.findByUser_Id(userId).orElse(null);
        }

        log.info("Profil diperbarui untuk user: {}", userId);
        return mapToResponse(user, client);
    }

    @Override
    @Transactional
    public void changePassword(UserPrincipal currentUser, ChangePasswordRequest request) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("Password lama tidak sesuai");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password berhasil diubah untuk user: {}", currentUser.getUserId());
    }

    // ── Private helpers ──────────────────────────────────────────────────────


    private UserProfileResponse mapToResponse(User user, Client client) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole().name(),
                client != null ? client.getStatusMember().name() : null,
                client != null ? client.getTotalPoint() : null,
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
