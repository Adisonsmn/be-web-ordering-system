package com.aromasenja.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stateful refresh token — disimpan di DB untuk support revoke/logout.
 * Mapping ke tabel public.refresh_token.
 *
 * Saat user logout atau refresh token digunakan untuk issue token baru,
 * token lama di-revoke (is_revoked = true).
 * Strategi: revoke semua token user lama saat login baru (single active session per user).
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id")
    private UUID tokenId;

    /**
     * FK ke users.id — refresh token hanya untuk member (bukan guest).
     * LAZY fetch karena user jarang diperlukan saat validasi token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Token value — JWT string, harus unique. */
    @Column(name = "token", nullable = false, unique = true, length = 2048)
    private String token;

    /** Timestamp expiry — cross-check dengan JWT expiry claim. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Flag revoke — token yang sudah direvoke tidak boleh digunakan lagi. */
    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
