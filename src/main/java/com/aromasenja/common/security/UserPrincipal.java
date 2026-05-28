package com.aromasenja.common.security;

import com.aromasenja.common.Role;
import com.aromasenja.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Implementasi UserDetails yang membawa informasi user terautentikasi.
 *
 * Ada tiga cara membuat UserPrincipal:
 * 1. UserPrincipal.from(User)       — dari entity DB (saat login / loadUserByUsername)
 * 2. UserPrincipal.fromClaims(...)  — reconstruct dari JWT claims (di JwtAuthFilter, non-guest)
 * 3. UserPrincipal.forGuest(...)    — untuk guest yang hanya punya JWT, tanpa record DB
 */
@Getter
public class UserPrincipal implements UserDetails {

    /** UUID dari users.id (untuk member) atau sessionId random (untuk guest). */
    private final UUID userId;

    /** Email user. Null untuk guest. */
    private final String email;

    /**
     * BCrypt password hash — hanya diisi saat loadUserByUsername (untuk DaoAuthenticationProvider).
     * Null saat reconstruct dari JWT claims (tidak diperlukan setelah login).
     */
    private final String password;

    /** Role pengguna: ADMIN atau CLIENT. */
    private final Role role;

    /** True jika pengguna adalah guest (scan QR tanpa login). */
    private final boolean isGuest;

    /**
     * UUID meja yang di-scan — hanya ada untuk guest.
     * Null untuk member yang sudah login.
     */
    private final UUID tableId;

    private UserPrincipal(UUID userId, String email, String password,
                          Role role, boolean isGuest, UUID tableId) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isGuest = isGuest;
        this.tableId = tableId;
    }

    // ── Static factories ─────────────────────────────────────────────────────

    /** Factory dari entity User di DB (digunakan oleh UserDetailsService dan setelah register). */
    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                false,
                null
        );
    }

    /**
     * Factory untuk reconstruct non-guest dari JWT claims.
     * Tidak memuat password karena tidak diperlukan setelah login.
     */
    public static UserPrincipal fromClaims(UUID userId, Role role) {
        return new UserPrincipal(userId, null, null, role, false, null);
    }

    /**
     * Factory untuk guest — dibuat dari JWT claims, tanpa query DB.
     *
     * @param sessionId UUID random yang menjadi sub JWT (bukan users.id)
     * @param tableId   UUID meja yang di-scan
     */
    public static UserPrincipal forGuest(UUID sessionId, UUID tableId) {
        return new UserPrincipal(sessionId, null, null, Role.CLIENT, true, tableId);
    }

    // ── UserDetails interface ─────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // Spring Security menggunakan getUsername() sebagai identifier
        return email != null ? email : userId.toString();
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    // ── Helper methods ───────────────────────────────────────────────────────

    /** Cek apakah user memiliki role tertentu (case-insensitive). */
    public boolean hasRole(String roleName) {
        return role.name().equalsIgnoreCase(roleName);
    }
}
