package com.aromasenja.domain.user.entity;

import com.aromasenja.common.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity utama untuk semua pengguna (admin & client).
 * Mapping ke tabel public.users.
 *
 * - password: SELALU BCrypt hash, tidak pernah plain text
 * - role: ADMIN atau CLIENT — auto-converted via Role.RoleAttributeConverter
 * - JANGAN tambahkan @Data — berbahaya untuk Hibernate lazy loading
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    /**
     * BCrypt hash password.
     * JANGAN pernah expose field ini via response API.
     * @JsonIgnore ditambahkan di Jackson level jika perlu serialisasi entity langsung
     * (sebaiknya gunakan DTO alih-alih serialize entity).
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Role: ADMIN atau CLIENT.
     * Auto-converted ke lowercase ("admin"/"client") oleh Role.RoleAttributeConverter.
     */
    @Column(name = "user_type", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
