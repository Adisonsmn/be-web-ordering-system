package com.aromasenja.domain.user;

import com.aromasenja.domain.user.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Repository untuk entity Client (tabel client). */
public interface ClientRepository extends JpaRepository<Client, UUID> {

    /**
     * Cari client berdasarkan users.id.
     * Digunakan saat perlu ambil profil client dari userId yang ada di JWT.
     */
    Optional<Client> findByUser_Id(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Client c JOIN c.user u WHERE " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<Client> searchMembers(
            @org.springframework.data.repository.query.Param("search") String search,
            org.springframework.data.domain.Pageable pageable);
}
