package com.aromasenja.domain.user;

import com.aromasenja.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Repository untuk entity RefreshToken (tabel refresh_token). */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Cari token berdasarkan nilai string-nya.
     * Digunakan saat validasi refresh request.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Revoke semua token aktif milik user — dipanggil saat login baru.
     * Strategi: single active session per user.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    /**
     * Revoke satu token spesifik — dipanggil saat logout.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.token = :token")
    void revokeByToken(@Param("token") String token);

    /**
     * Cek apakah token ID tertentu ada dan belum di-revoke (masih aktif).
     */
    boolean existsByTokenIdAndIsRevokedFalse(java.util.UUID tokenId);
}
