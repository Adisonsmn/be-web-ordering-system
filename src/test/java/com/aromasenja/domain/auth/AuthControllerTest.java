package com.aromasenja.domain.auth;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.auth.dto.LoginRequest;
import com.aromasenja.domain.auth.dto.LoginResponse;
import com.aromasenja.domain.auth.dto.RefreshTokenRequest;
import com.aromasenja.domain.auth.dto.RegisterRequest;
import com.aromasenja.domain.user.UserService;
import com.aromasenja.domain.user.dto.UpdateProfileRequest;
import com.aromasenja.domain.user.dto.UserProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test untuk AuthController.
 *
 * Strategi testing:
 * - @WebMvcTest: load hanya layer web (Controller + Validation + Security auto-config)
 * - Tidak import SecurityConfig custom — gunakan Spring Boot Test default security
 *   agar tidak bergantung pada CorsConfig / bean config yang kompleks
 * - @Import(GlobalExceptionHandler): agar error handling @Valid dan exception bekerja benar
 * - with(csrf()): dibutuhkan untuk POST/PUT karena Spring Test mengaktifkan CSRF by default
 * - @WithMockUser: untuk endpoint yang butuh autentikasi (GET/PUT /me)
 */
@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController MockMvc Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private UserService userService;
    @MockBean private JwtService jwtService; // diperlukan oleh JwtAuthFilter yang di-load otomatis

    // ── Test fixtures ─────────────────────────────────────────────────────────

    private UUID userId;
    private UserProfileResponse mockProfile;
    private LoginResponse mockLoginResponse;
    private UsernamePasswordAuthenticationToken mockClientAuth; // untuk inject UserPrincipal ke test

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        mockProfile = new UserProfileResponse(
                userId,
                "client@aromasenja.com",
                "Client User",
                "081234567890",
                "CLIENT",
                "REGULAR",
                150,
                null,
                LocalDateTime.now()
        );

        mockLoginResponse = new LoginResponse(
                "mock-access-token",
                "mock-refresh-token",
                "Bearer",
                900L,
                mockProfile
        );

        // Buat UserPrincipal yang valid agar @AuthenticationPrincipal di controller tidak null
        UserPrincipal clientPrincipal = UserPrincipal.fromClaims(userId, Role.CLIENT);
        mockClientAuth = new UsernamePasswordAuthenticationToken(
                clientPrincipal, null, clientPrincipal.getAuthorities());
    }

    // ── POST /api/auth/register ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register — 201 Created saat body valid")
    @WithMockUser
    void POST_register_201_sukses() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "client@aromasenja.com", "Password123!", "Client User", "081234567890"
        );

        when(authService.register(any())).thenReturn(mockLoginResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.user.email").value("client@aromasenja.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register — 400 Bad Request saat email kosong")
    @WithMockUser
    void POST_register_400_emailKosong() throws Exception {
        String invalidJson = """
                {
                    "email": "",
                    "password": "Password123!",
                    "name": "Test User"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login — 200 OK saat kredensial valid")
    @WithMockUser
    void POST_login_200_sukses() throws Exception {
        LoginRequest request = new LoginRequest("client@aromasenja.com", "Password123!");

        when(authService.login(any())).thenReturn(mockLoginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login — 400 Bad Request saat password kosong")
    @WithMockUser
    void POST_login_400_passwordKosong() throws Exception {
        String invalidJson = """
                {
                    "email": "client@aromasenja.com",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── POST /api/auth/refresh ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/refresh — 200 OK saat refresh token valid")
    @WithMockUser
    void POST_refresh_200_sukses() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        LoginResponse refreshedResponse = new LoginResponse(
                "new-access-token", "valid-refresh-token", "Bearer", 900L, mockProfile
        );
        when(authService.refreshToken(any())).thenReturn(refreshedResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    // ── POST /api/auth/logout ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/logout — 200 OK saat refresh token ada di body")
    @WithMockUser
    void POST_logout_200_sukses() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout berhasil"));
    }

    // ── GET /api/auth/me ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/auth/me — 200 OK saat user terautentikasi")
    void GET_me_200_sukses() throws Exception {
        when(userService.getProfile(any())).thenReturn(mockProfile);

        // Inject UserPrincipal nyata via authentication() agar @AuthenticationPrincipal bekerja
        mockMvc.perform(get("/api/auth/me")
                        .with(authentication(mockClientAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("client@aromasenja.com"))
                .andExpect(jsonPath("$.data.role").value("CLIENT"))
                .andExpect(jsonPath("$.data.totalPoint").value(150));
    }

    @Test
    @DisplayName("GET /api/auth/me — 401 Unauthorized tanpa token")
    void GET_me_401_tanpaToken() throws Exception {
        // Tanpa @WithMockUser → Spring Security default menolak → 401
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/auth/me ──────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/auth/me — 200 OK saat body valid dan user terautentikasi")
    void PUT_me_200_sukses() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Nama Baru",
                "089876543210",
                "https://cdn.aromasenja.com/avatar/new.jpg"
        );

        UserProfileResponse updatedProfile = new UserProfileResponse(
                userId, "client@aromasenja.com", "Nama Baru", "089876543210",
                "CLIENT", "REGULAR", 150,
                "https://cdn.aromasenja.com/avatar/new.jpg", LocalDateTime.now()
        );
        when(userService.updateProfile(any(), any(), any())).thenReturn(updatedProfile);

        // Inject UserPrincipal nyata via authentication() agar currentUser.getUserId() tidak NPE
        mockMvc.perform(put("/api/auth/me")
                        .with(authentication(mockClientAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Nama Baru"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://cdn.aromasenja.com/avatar/new.jpg"));
    }

    @Test
    @DisplayName("PUT /api/auth/me — 400 Bad Request saat nama kosong")
    void PUT_me_400_namaKosong() throws Exception {
        String invalidJson = """
                {
                    "name": "",
                    "phone": "081234567890"
                }
                """;

        // Inject auth agar tidak ditolak security, fokus test validasi @Valid
        mockMvc.perform(put("/api/auth/me")
                        .with(authentication(mockClientAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
