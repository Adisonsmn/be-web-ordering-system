package com.aromasenja.domain.auth;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.auth.dto.LoginRequest;
import com.aromasenja.domain.auth.dto.LoginResponse;
import com.aromasenja.domain.auth.dto.RefreshTokenRequest;
import com.aromasenja.domain.auth.dto.RegisterRequest;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.RefreshTokenRepository;
import com.aromasenja.domain.user.UserRepository;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.RefreshToken;
import com.aromasenja.domain.user.entity.StatusMember;
import com.aromasenja.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test untuk AuthServiceImpl.
 * Semua dependency di-mock — tidak ada koneksi DB atau Spring context.
 * Menggunakan JUnit 5 + Mockito + AssertJ.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    // ── Test fixtures ─────────────────────────────────────────────────────────

    private UUID userId;
    private User mockUser;
    private Client mockClient;
    private UserPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        // Set nilai @Value yang tidak bisa di-inject otomatis oleh Mockito
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 604800000L); // 7 hari

        userId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@aromasenja.com");
        mockUser.setName("Test User");
        mockUser.setPhone("081234567890");
        mockUser.setPassword("$2a$encoded_password");
        mockUser.setRole(Role.CLIENT);

        mockClient = new Client();
        mockClient.setUser(mockUser);
        mockClient.setStatusMember(StatusMember.REGULAR);
        mockClient.setTotalPoint(100);

        mockPrincipal = UserPrincipal.from(mockUser);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login sukses — kredensial valid mengembalikan JWT pair dan profil")
    void login_sukses_returnLoginResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("test@aromasenja.com", "password123");
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(mockPrincipal, null, mockPrincipal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("mock-refresh-token");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-access-token");
        assertThat(response.refreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.user()).isNotNull();
        assertThat(response.user().email()).isEqualTo("test@aromasenja.com");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("login gagal — kredensial salah melempar BadCredentialsException")
    void login_gagal_credentialSalah() {
        // Arrange
        LoginRequest request = new LoginRequest("test@aromasenja.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateAccessToken(any(), any());
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register sukses — email baru membuat user + client dan return JWT pair")
    void register_sukses_emailBaru() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "budi@aromasenja.com", "Password123!", "Budi Santoso", "081234567890"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            UUID newId = UUID.randomUUID();
            u.setId(newId);
            // Stub findById agar saveRefreshToken bisa menemukan user yang baru disave
            when(userRepository.findById(newId)).thenReturn(Optional.of(u));
            return u;
        });
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("mock-refresh-token");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        LoginResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-access-token");
        assertThat(response.user().email()).isEqualTo("budi@aromasenja.com");
        assertThat(response.user().name()).isEqualTo("Budi Santoso");
        verify(userRepository).save(any(User.class));
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("register gagal — email sudah ada melempar BusinessException")
    void register_gagal_emailSudahAda() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "ada@aromasenja.com", "Password123!", "Duplikat", null
        );
        when(userRepository.existsByEmail("ada@aromasenja.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email sudah terdaftar");

        verify(userRepository, never()).save(any());
        verify(clientRepository, never()).save(any());
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("refreshToken sukses — token valid mengembalikan access token baru")
    void refreshToken_sukses_tokenValid() {
        // Arrange
        String rawToken = "valid-refresh-token";
        RefreshToken storedToken = buildValidRefreshToken(rawToken);
        RefreshTokenRequest request = new RefreshTokenRequest(rawToken);

        when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("new-access-token");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));

        // Act
        LoginResponse response = authService.refreshToken(request);

        // Assert
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo(rawToken); // refresh token tidak dirotasi
    }

    @Test
    @DisplayName("refreshToken gagal — token sudah direvoke melempar BusinessException")
    void refreshToken_gagal_tokenRevoked() {
        // Arrange
        String rawToken = "revoked-token";
        RefreshToken revokedToken = buildValidRefreshToken(rawToken);
        revokedToken.setRevoked(true);
        RefreshTokenRequest request = new RefreshTokenRequest(rawToken);

        when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(revokedToken));

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tidak aktif");
    }

    @Test
    @DisplayName("refreshToken gagal — token sudah expired melempar BusinessException")
    void refreshToken_gagal_tokenExpired() {
        // Arrange
        String rawToken = "expired-token";
        RefreshToken expiredToken = buildValidRefreshToken(rawToken);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // expired kemarin
        RefreshTokenRequest request = new RefreshTokenRequest(rawToken);

        when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired");
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout sukses — refresh token di-revoke di DB")
    void logout_sukses_tokenDirevoke() {
        // Arrange
        String rawToken = "valid-refresh-token";
        RefreshToken storedToken = buildValidRefreshToken(rawToken);
        when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(storedToken));

        // Act
        authService.logout(rawToken);

        // Assert
        verify(refreshTokenRepository).revokeByToken(rawToken);
    }

    @Test
    @DisplayName("logout — token tidak ada di DB tidak melempar exception")
    void logout_tokenTidakAda_tidakThrow() {
        // Arrange
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert — tidak boleh throw
        assertThatCode(() -> authService.logout("token-tidak-ada"))
                .doesNotThrowAnyException();

        verify(refreshTokenRepository, never()).revokeByToken(anyString());
    }

    // ── Helper privat ─────────────────────────────────────────────────────────

    /**
     * Membuat objek RefreshToken yang valid (belum expired, belum direvoke).
     */
    private RefreshToken buildValidRefreshToken(String tokenValue) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(mockUser);
        rt.setToken(tokenValue);
        rt.setExpiresAt(LocalDateTime.now().plusDays(7)); // valid 7 hari ke depan
        rt.setRevoked(false);
        return rt;
    }
}
