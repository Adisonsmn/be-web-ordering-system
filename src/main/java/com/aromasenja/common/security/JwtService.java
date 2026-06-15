package com.aromasenja.common.security;

import com.aromasenja.common.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service untuk generate dan validasi JWT menggunakan JJWT 0.12.x API.
 *
 * JWT payload:
 * - sub  : userId (UUID string) untuk member, sessionId (UUID string) untuk guest
 * - role : "admin" atau "client" (lowercase, sesuai DB)
 * - isGuest : boolean
 * - tableId : UUID string meja (hanya untuk guest)
 */
@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    /** Signing key dari secret Base64 yang dikonfigurasi di application.yml. */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Token generation ─────────────────────────────────────────────────────

    /** Generate access token untuk user yang sudah login (non-guest). */
    public String generateAccessToken(UserPrincipal principal, UUID tokenId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", principal.getRole().toDbValue());
        claims.put("isGuest", false);
        if (tokenId != null) {
            claims.put("tokenId", tokenId.toString());
        }
        return buildToken(claims, principal.getUserId().toString(), accessTokenExpiry);
    }

    /** Generate refresh token (payload minimal — hanya sub). */
    public String generateRefreshToken(UserPrincipal principal) {
        return buildToken(new HashMap<>(), principal.getUserId().toString(), refreshTokenExpiry);
    }

    /**
     * Generate access token untuk guest.
     *
     * @param sessionId UUID random sebagai identitas guest session
     * @param tableId   UUID meja yang di-scan
     */
    public String generateGuestToken(UUID sessionId, UUID tableId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "client");
        claims.put("isGuest", true);
        claims.put("tableId", tableId.toString());
        return buildToken(claims, sessionId.toString(), accessTokenExpiry);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiryMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expiryMs);
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token extraction ─────────────────────────────────────────────────────

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public Role extractRole(String token) {
        String roleStr = extractAllClaims(token).get("role", String.class);
        return Role.fromDbValue(roleStr);
    }

    public boolean extractIsGuest(String token) {
        Boolean isGuest = extractAllClaims(token).get("isGuest", Boolean.class);
        return Boolean.TRUE.equals(isGuest);
    }

    public UUID extractTableId(String token) {
        String tableIdStr = extractAllClaims(token).get("tableId", String.class);
        return tableIdStr != null ? UUID.fromString(tableIdStr) : null;
    }

    public UUID extractTokenId(String token) {
        String tokenIdStr = extractAllClaims(token).get("tokenId", String.class);
        return tokenIdStr != null ? UUID.fromString(tokenIdStr) : null;
    }

    // ── Token validation ─────────────────────────────────────────────────────

    /**
     * Validasi token: signature valid dan belum expired.
     * Log peringatan jika token tidak valid (jangan log token mentah — data sensitif).
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT tidak valid: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
