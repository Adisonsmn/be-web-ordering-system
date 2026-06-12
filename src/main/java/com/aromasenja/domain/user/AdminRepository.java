package com.aromasenja.domain.user;

import com.aromasenja.domain.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Repository untuk entity Admin (tabel admin). */
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    /**
     * Cari admin berdasarkan users.id.
     * Digunakan untuk validasi admin saat update config atau manajemen operasional.
     */
    Optional<Admin> findByUser_Id(UUID userId);
}
