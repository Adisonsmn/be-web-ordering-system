package com.aromasenja.common.security;

import com.aromasenja.common.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter JWT yang dijalankan sekali per request.
 * Membaca Authorization header, validasi token, lalu set authentication ke SecurityContext.
 *
 * Catatan: Filter ini TIDAK query DB — UserPrincipal di-reconstruct langsung dari JWT claims.
 * Ini adalah pendekatan stateless. Untuk validasi token yang sudah di-revoke,
 * gunakan endpoint /api/auth/refresh yang mengecek DB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null && jwtService.isTokenValid(token)) {
            try {
                UUID userId    = jwtService.extractUserId(token);
                Role role      = jwtService.extractRole(token);
                boolean isGuest = jwtService.extractIsGuest(token);

                UserPrincipal principal;
                if (isGuest) {
                    UUID tableId = jwtService.extractTableId(token);
                    principal = UserPrincipal.forGuest(userId, tableId);
                } else {
                    principal = UserPrincipal.fromClaims(userId, role);
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Jangan throw — biarkan chain berlanjut, Spring Security akan menolak request
                log.warn("Gagal memproses JWT token pada request ke {}: {}",
                        request.getRequestURI(), e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Ekstrak token dari header "Authorization: Bearer <token>". */
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
