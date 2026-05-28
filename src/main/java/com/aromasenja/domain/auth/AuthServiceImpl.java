package com.aromasenja.domain.auth;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.auth.dto.*;
import com.aromasenja.domain.user.*;
import com.aromasenja.domain.user.dto.UserProfileResponse;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.RefreshToken;
import com.aromasenja.domain.user.entity.StatusMember;
import com.aromasenja.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementasi AuthService — menangani semua alur autentikasi.
 *
 * Strategi refresh token:
 * - Saat login baru: revoke semua token lama (single active session)
 * - Saat refresh: token lama tetap valid (tidak di-revoke) sampai expired
 * - Saat logout: revoke token yang dikirim saja
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiryMs;

    // ── Login ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Spring Security akan memanggil UserDetailsService.loadUserByUsername
        // dan verifikasi password dengan BCrypt
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().trim().toLowerCase(),
                        request.password()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Login berhasil untuk user: {}", principal.getUserId());

        String accessToken  = jwtService.generateAccessToken(principal);
        String refreshToken = saveRefreshToken(principal);
        UserProfileResponse userProfile = buildUserProfile(principal.getUserId());

        return new LoginResponse(accessToken, refreshToken, "Bearer",
                refreshTokenExpiryMs / 1000, userProfile);
    }

    // ── Register ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Cek duplikasi email sebelum insert
        if (userRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new BusinessException("Email sudah terdaftar: " + request.email());
        }

        // Buat User baru
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setName(request.name().trim());
        user.setPhone(request.phone() != null ? request.phone().trim() : null);
        user.setPassword(passwordEncoder.encode(request.password())); // BCrypt hash
        user.setRole(Role.CLIENT);
        user = userRepository.save(user);

        // Buat Client profile
        Client client = new Client();
        client.setUser(user);
        client.setStatusMember(StatusMember.REGULAR);
        client.setTotalPoint(0);
        clientRepository.save(client);

        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken  = jwtService.generateAccessToken(principal);
        String refreshToken = saveRefreshToken(principal);

        log.info("Registrasi berhasil untuk user: {}", user.getId());
        UserProfileResponse userProfile = buildUserProfileFromEntity(user, client);
        return new LoginResponse(accessToken, refreshToken, "Bearer",
                refreshTokenExpiryMs / 1000, userProfile);
    }

    // ── Refresh Token ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token tidak ditemukan atau tidak valid"));

        if (storedToken.isRevoked()) {
            throw new BusinessException("Refresh token sudah tidak aktif. Silakan login ulang.");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token sudah expired. Silakan login ulang.");
        }

        User user = storedToken.getUser();
        UserPrincipal principal = UserPrincipal.from(user);
        String newAccessToken = jwtService.generateAccessToken(principal);

        log.info("Access token di-refresh untuk user: {}", user.getId());
        UserProfileResponse userProfile = buildUserProfile(user.getId());

        // Refresh token TIDAK di-revoke — masih bisa digunakan sampai expired
        return new LoginResponse(newAccessToken, request.refreshToken(), "Bearer",
                refreshTokenExpiryMs / 1000, userProfile);
    }

    // ── Guest Login ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse loginAsGuest(GuestLoginRequest request) {
        // TODO: Validasi mejaId ada di DB — akan diaktifkan saat domain/meja selesai
        // MejaRepository akan di-inject dan dilakukan: mejaRepository.findById(request.tableId())
        //     .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan"));

        UUID sessionId = UUID.randomUUID();
        String guestToken = jwtService.generateGuestToken(sessionId, request.tableId());

        // Guest tidak mendapat refresh token (Pure JWT — tidak ada sesi persisten)
        log.info("Guest login untuk meja: {}, sessionId: {}", request.tableId(), sessionId);
        return new LoginResponse(guestToken, null, "Bearer",
                refreshTokenExpiryMs / 1000, null);
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresentOrElse(
                        token -> {
                            refreshTokenRepository.revokeByToken(refreshToken);
                            log.info("Logout berhasil, token direvoke untuk user: {}",
                                    token.getUser().getId());
                        },
                        () -> log.warn("Percobaan logout dengan token yang tidak ditemukan di DB")
                );
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Simpan refresh token baru ke DB.
     * Semua token lama user di-revoke terlebih dahulu (single session strategy).
     */
    private String saveRefreshToken(UserPrincipal principal) {
        // Revoke semua token aktif user ini
        refreshTokenRepository.revokeAllByUserId(principal.getUserId());

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        String rawRefreshToken = jwtService.generateRefreshToken(principal);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(rawRefreshToken);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000));
        refreshTokenRepository.save(refreshToken);

        return rawRefreshToken;
    }

    private UserProfileResponse buildUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        Client client = null;
        if (user.getRole() == Role.CLIENT) {
            client = clientRepository.findByUser_Id(userId).orElse(null);
        }
        return buildUserProfileFromEntity(user, client);
    }

    private UserProfileResponse buildUserProfileFromEntity(User user, Client client) {
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
