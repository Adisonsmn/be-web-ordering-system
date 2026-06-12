package com.aromasenja.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Profil tambahan khusus pelanggan (client/member).
 * Mapping ke tabel public.client.
 *
 * Relasi OneToOne ke User (tabel induk).
 * Strategi guest: Pure JWT (Pilihan B) — guest TIDAK punya record di tabel ini.
 * Semua client di DB ini adalah member terdaftar (REGULAR atau PREMIUM).
 */
@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "client_id")
    private UUID clientId;

    /**
     * FK ke users.id.
     * Nullable di schema (untuk kompatibilitas schema), tapi dalam aplikasi ini
     * selalu diset karena guest menggunakan Pure JWT (tidak ada client row untuk guest).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", unique = true)
    private User user;

    /**
     * Status keanggotaan.
     * Auto-converted ke lowercase oleh StatusMember.StatusMemberConverter.
     * Default: REGULAR saat registrasi.
     */
    @Column(name = "status_member", nullable = false)
    private StatusMember statusMember = StatusMember.REGULAR;

    @Column(name = "total_point", nullable = false)
    private Integer totalPoint = 0;

    @Version
    private Long version;
}
