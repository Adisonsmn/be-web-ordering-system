package com.aromasenja.domain.user;

import com.aromasenja.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository untuk entity User (tabel users).
 * Method naming convention digunakan untuk query sederhana — tidak perlu @Query.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Cari user berdasarkan email — digunakan saat login (loadUserByUsername). */
    Optional<User> findByEmail(String email);

    /** Cek duplikasi email saat registrasi — lebih efisien dari findBy().isPresent(). */
    boolean existsByEmail(String email);
}
