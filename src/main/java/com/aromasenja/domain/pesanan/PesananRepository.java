package com.aromasenja.domain.pesanan;

import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PesananRepository extends JpaRepository<Pesanan, UUID> {

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

    @Query(value = "SELECT p FROM Pesanan p LEFT JOIN FETCH p.meja m LEFT JOIN FETCH p.client c WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:mejaId IS NULL OR p.meja.mejaId = :mejaId) AND " +
           "(:tanggal IS NULL OR CAST(p.tanggalPesanan AS date) = :tanggal)",
           countQuery = "SELECT COUNT(p) FROM Pesanan p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:mejaId IS NULL OR p.meja.mejaId = :mejaId) AND " +
           "(:tanggal IS NULL OR CAST(p.tanggalPesanan AS date) = :tanggal)")
    Page<Pesanan> findAllAdminFiltered(
            @Param("status") StatusPesanan status,
            @Param("mejaId") UUID mejaId,
            @Param("tanggal") LocalDate tanggal,
            Pageable pageable);

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
