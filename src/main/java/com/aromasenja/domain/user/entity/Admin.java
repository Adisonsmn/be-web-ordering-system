package com.aromasenja.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Profil tambahan khusus operator restoran (admin).
 * Mapping ke tabel public.admin.
 *
 * Relasi OneToOne ke User (tabel induk).
 */
@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "admin_id")
    private UUID adminId;

    /**
     * FK ke users.id. NOT NULL — setiap admin harus punya user account.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false, unique = true)
    private User user;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Shift kerja admin.
     * Auto-converted ke lowercase oleh ShiftAdmin.ShiftAdminConverter.
     * Nullable — admin bisa belum ditetapkan shiftnya.
     */
    @Column(name = "work_shift")
    private ShiftAdmin workShift;
}
