package com.aromasenja.domain.pesanan;

import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DetailPesananRepository extends JpaRepository<DetailPesanan, UUID> {

    @Query("SELECT COUNT(dp) > 0 FROM DetailPesanan dp WHERE dp.menu.promo.promoId = :promoId")
    boolean existsByMenuPromoPromoId(@Param("promoId") UUID promoId);

    /**
     * Menu terlaris berdasarkan total qty SERVED dalam rentang waktu.
     * Limit diterapkan via LIMIT di native query agar tidak butuh Pageable.
     */
    @Query(value =
            "SELECT dp.menu_id AS menuId, m.menu_name AS menuName, " +
            "       SUM(dp.quantity) AS totalQty, SUM(dp.sub_total) AS totalIncome " +
            "FROM detail_pesanan dp " +
            "JOIN pesanan p ON dp.pesanan_id = p.pesanan_id " +
            "JOIN menus m ON dp.menu_id = m.menu_id " +
            "WHERE UPPER(p.status) = 'SERVED' " +
            "  AND p.tanggal_pesanan BETWEEN :start AND :end " +
            "  AND (:category IS NULL OR UPPER(m.category) = UPPER(:category)) " +
            "GROUP BY dp.menu_id, m.menu_name " +
            "ORDER BY SUM(dp.quantity) DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Map<String, Object>> getMenuTerlarisAggregated(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") String category,
            @Param("limit") int limit);

    @Query("SELECT dp.menu.promo.promoId AS promoId, dp.menu.promo.namaPromo AS namaPromo, COUNT(dp) AS count " +
           "FROM DetailPesanan dp JOIN dp.pesanan p " +
           "WHERE p.status = com.aromasenja.domain.pesanan.entity.StatusPesanan.SERVED " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon " +
           "AND dp.menu.promo IS NOT NULL " +
           "GROUP BY dp.menu.promo.promoId, dp.menu.promo.namaPromo " +
           "ORDER BY COUNT(dp) DESC")
    List<Map<String, Object>> getPromoUsageStatsAggregated();

    @Query("SELECT COALESCE(SUM((dp.hargaSnapshot - dp.hargaSetelahDiskon) * dp.quantity), 0) " +
           "FROM DetailPesanan dp JOIN dp.pesanan p " +
           "WHERE p.status = com.aromasenja.domain.pesanan.entity.StatusPesanan.SERVED " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon")
    BigDecimal getTotalDiskonPromo();

    @Query("SELECT COALESCE(SUM((dp.hargaSnapshot - dp.hargaSetelahDiskon) * dp.quantity), 0) " +
           "FROM DetailPesanan dp JOIN dp.pesanan p " +
           "WHERE p.status = com.aromasenja.domain.pesanan.entity.StatusPesanan.SERVED " +
           "AND p.tanggalPesanan BETWEEN :start AND :end " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon")
    BigDecimal getTotalDiskonPromoBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT dp.pesanan.pesananId) " +
           "FROM DetailPesanan dp JOIN dp.pesanan p " +
           "WHERE p.status = com.aromasenja.domain.pesanan.entity.StatusPesanan.SERVED " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon")
    long getTotalPesananPakaiPromo();
}
