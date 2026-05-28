package com.aromasenja.domain.pesanan;

import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface DetailPesananRepository extends JpaRepository<DetailPesanan, UUID> {

    @Query("SELECT COUNT(dp) > 0 FROM DetailPesanan dp WHERE dp.menu.promo.promoId = :promoId")
    boolean existsByMenuPromoPromoId(@Param("promoId") UUID promoId);

    @Query("SELECT dp.menu.menuId AS menuId, dp.menu.menuName AS menuName, SUM(dp.quantity) AS totalQty, SUM(dp.subTotal) AS totalIncome " +
           "FROM DetailPesanan dp JOIN dp.pesanan p WHERE p.status = 'SERVED' " +
           "AND p.tanggalPesanan BETWEEN :start AND :end " +
           "AND (:category IS NULL OR dp.menu.category = :category) " +
           "GROUP BY dp.menu.menuId, dp.menu.menuName " +
           "ORDER BY SUM(dp.quantity) DESC")
    java.util.List<java.util.Map<String, Object>> getMenuTerlarisAggregated(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end,
            @Param("category") String category,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT dp.menu.promo.promoId AS promoId, dp.menu.promo.namaPromo AS namaPromo, COUNT(dp) AS count " +
           "FROM DetailPesanan dp JOIN dp.pesanan p WHERE p.status = 'SERVED' " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon " +
           "AND dp.menu.promo IS NOT NULL " +
           "GROUP BY dp.menu.promo.promoId, dp.menu.promo.namaPromo " +
           "ORDER BY COUNT(dp) DESC")
    java.util.List<java.util.Map<String, Object>> getPromoUsageStatsAggregated();

    @Query("SELECT COALESCE(SUM((dp.hargaSnapshot - dp.hargaSetelahDiskon) * dp.quantity), 0) " +
           "FROM DetailPesanan dp JOIN dp.pesanan p WHERE p.status = 'SERVED' " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon")
    java.math.BigDecimal getTotalDiskonPromo();

    @Query("SELECT COALESCE(SUM((dp.hargaSnapshot - dp.hargaSetelahDiskon) * dp.quantity), 0) " +
           "FROM DetailPesanan dp JOIN dp.pesanan p WHERE p.status = 'SERVED' " +
           "AND p.tanggalPesanan BETWEEN :start AND :end " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon")
    java.math.BigDecimal getTotalDiskonPromoBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT dp.pesanan.pesananId) " +
           "FROM DetailPesanan dp JOIN dp.pesanan p WHERE p.status = 'SERVED' " +
           "AND dp.hargaSnapshot > dp.hargaSetelahDiskon")
    long getTotalPesananPakaiPromo();
}
