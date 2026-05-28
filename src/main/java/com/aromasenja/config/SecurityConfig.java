package com.aromasenja.config;

import com.aromasenja.common.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Konfigurasi Spring Security.
 *
 * - Stateless (JWT): session tidak disimpan di server
 * - CSRF dinonaktifkan (tidak relevan untuk stateless API)
 * - Public endpoints di-whitelist sesuai security-standart.md
 * - JwtAuthFilter berjalan sebelum UsernamePasswordAuthenticationFilter
 * - @PreAuthorize aktif via @EnableMethodSecurity
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Auth: endpoint PUBLIC (tidak butuh JWT) ──────────────────
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/auth/guest",
                        "/api/auth/logout"
                ).permitAll()

                // ── Menu catalog & ratings (GET public) ──────────────────────
                .requestMatchers(HttpMethod.GET, "/api/menu", "/api/menu/{id}", "/api/menu/{id}/pairings", "/api/rating/menu/**").permitAll()

                // ── Scan QR meja (public) ────────────────────────────────────
                .requestMatchers("/api/meja/scan/**").permitAll()

                // ── Promo aktif (GET public) ─────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/promo").permitAll()

                // ── Config restoran (GET public) ─────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/config").permitAll()

                // ── WebSocket handshake (public) ─────────────────────────────
                .requestMatchers("/ws/**").permitAll()

                // ── Swagger & API docs (public) ──────────────────────────────
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                ).permitAll()

                // ── Actuator & Health Check (public) ─────────────────────────
                .requestMatchers("/api/health", "/actuator/health", "/actuator/info").permitAll()

                // ── Semua endpoint lain butuh autentikasi ────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCryptPasswordEncoder dengan strength 12.
     * Digunakan oleh AuthServiceImpl untuk hash password dan oleh
     * DaoAuthenticationProvider untuk verifikasi saat login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * AuthenticationManager bean — dibutuhkan oleh AuthServiceImpl.authenticate().
     * Spring Boot auto-configures DaoAuthenticationProvider menggunakan
     * UserDetailsService (dari UserServiceImpl) dan PasswordEncoder (dari bean di atas).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
