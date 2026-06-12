package com.aromasenja.domain.promo;

import com.aromasenja.domain.promo.entity.Promo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PromoRepository extends JpaRepository<Promo, UUID> {

    // Untuk client: Promo aktif
    List<Promo> findByIsActiveTrueAndTanggalMulaiLessThanEqualAndTanggalSelesaiGreaterThanEqual(
            LocalDate dateForMulai, LocalDate dateForSelesai);

    // Untuk admin: filter active
    @Query("SELECT p FROM Promo p WHERE p.isActive = true AND p.tanggalMulai <= :today AND p.tanggalSelesai >= :today")
    List<Promo> findActivePromos(@Param("today") LocalDate today);

    // Untuk admin: filter scheduled
    @Query("SELECT p FROM Promo p WHERE p.isActive = true AND p.tanggalMulai > :today")
    List<Promo> findScheduledPromos(@Param("today") LocalDate today);

    // Untuk admin: filter ended
    @Query("SELECT p FROM Promo p WHERE p.isActive = false OR p.tanggalSelesai < :today")
    List<Promo> findEndedPromos(@Param("today") LocalDate today);
}

