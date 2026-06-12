package com.aromasenja.domain.pesanan;

import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PesananRepository extends JpaRepository<Pesanan, UUID>, JpaSpecificationExecutor<Pesanan> {

    @Query("SELECT DISTINCT p FROM Pesanan p " +
           "LEFT JOIN FETCH p.detailPesanan dp " +
           "LEFT JOIN FETCH dp.menu " +
           "LEFT JOIN FETCH p.meja " +
           "LEFT JOIN FETCH p.client " +
           "WHERE p.pesananId = :id")
    Optional<Pesanan> findByIdWithDetails(@Param("id") UUID id);

    @Query(value = "SELECT p FROM Pesanan p LEFT JOIN FETCH p.meja LEFT JOIN FETCH p.client WHERE p.client.clientId = :clientId ORDER BY p.tanggalPesanan DESC",
           countQuery = "SELECT COUNT(p) FROM Pesanan p WHERE p.client.clientId = :clientId")
    Page<Pesanan> findByClientClientIdOrderByTanggalPesananDesc(@Param("clientId") UUID clientId, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Pesanan p " +
           "JOIN p.detailPesanan dp " +
           "LEFT JOIN FETCH p.client " +
           "LEFT JOIN FETCH p.meja " +
           "WHERE dp.menu.promo.promoId = :promoId " +
           "ORDER BY p.tanggalPesanan DESC",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Pesanan p " +
           "JOIN p.detailPesanan dp " +
           "WHERE dp.menu.promo.promoId = :promoId")
    Page<Pesanan> findByPromoId(@Param("promoId") UUID promoId, Pageable pageable);

    // findAllAdminFiltered dihapus — digantikan oleh PesananSpecification + JpaSpecificationExecutor
    // Gunakan pesananRepository.findAll(PesananSpecification.buildFilter(...), pageable)

    @Query("SELECT COALESCE(SUM(p.totalHarga), 0) FROM Pesanan p WHERE p.status = :status AND p.tanggalPesanan BETWEEN :start AND :end")
    BigDecimal sumTotalHargaByStatusAndTanggalPesananBetween(
            @Param("status") StatusPesanan status,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Pesanan p WHERE p.tanggalPesanan BETWEEN :start AND :end")
    long countByTanggalPesananBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT p FROM Pesanan p LEFT JOIN FETCH p.meja LEFT JOIN FETCH p.client WHERE p.status IN (:statuses)")
    List<Pesanan> findByStatusIn(
            @Param("statuses") java.util.Collection<StatusPesanan> statuses);

    @Query("SELECT p FROM Pesanan p LEFT JOIN FETCH p.meja LEFT JOIN FETCH p.client WHERE p.status = :status ORDER BY p.tanggalPesanan ASC LIMIT 20")
    List<Pesanan> findTop20ByStatusOrderByTanggalPesananAsc(@Param("status") StatusPesanan status);

    @Query("SELECT p FROM Pesanan p WHERE p.tanggalPesanan BETWEEN :start AND :end")
    java.util.List<Pesanan> findByTanggalPesananBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT MAX(p.tanggalPesanan) FROM Pesanan p WHERE p.client.clientId = :clientId")
    java.time.LocalDateTime findLastOrderTimeByClientId(@Param("clientId") UUID clientId);

    @Query("SELECT COUNT(p) FROM Pesanan p WHERE p.tanggalPesanan BETWEEN :start AND :end AND p.status <> com.aromasenja.domain.pesanan.entity.StatusPesanan.CANCELLED")
    long countNonCancelledByTanggalPesananBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT p.meja.mejaId) FROM Pesanan p WHERE p.tanggalPesanan BETWEEN :start AND :end AND p.status <> com.aromasenja.domain.pesanan.entity.StatusPesanan.CANCELLED")
    long countDistinctMejaByTanggalPesananBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);
    @Query("SELECT FUNCTION('date_trunc', 'day', p.tanggalPesanan) as tanggal, " +
           "SUM(p.totalHarga) as totalPendapatan, COUNT(p) as totalPesanan " +
           "FROM Pesanan p " +
           "WHERE p.status = com.aromasenja.domain.pesanan.entity.StatusPesanan.SERVED " +
           "AND p.tanggalPesanan BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('date_trunc', 'day', p.tanggalPesanan) " +
           "ORDER BY FUNCTION('date_trunc', 'day', p.tanggalPesanan) ASC")
    List<Object[]> findDailyRevenueBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT FUNCTION('date_trunc', 'month', p.tanggalPesanan) as bulan, " +
           "SUM(p.totalHarga) as totalPendapatan, COUNT(p) as totalPesanan " +
           "FROM Pesanan p " +
           "WHERE p.status = com.aromasenja.domain.pesanan.entity.StatusPesanan.SERVED " +
           "AND p.tanggalPesanan BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('date_trunc', 'month', p.tanggalPesanan) " +
           "ORDER BY FUNCTION('date_trunc', 'month', p.tanggalPesanan) ASC")
    List<Object[]> findMonthlyRevenueBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);
}
