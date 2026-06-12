package com.aromasenja.domain.poin;

import com.aromasenja.domain.poin.entity.PoinTransaksi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface PoinTransaksiRepository extends JpaRepository<PoinTransaksi, UUID> {

    Page<PoinTransaksi> findByClientClientIdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(jumlah_poin), 0) FROM poin_transaksi WHERE tipe = 'redeem'", nativeQuery = true)
    int getTotalPointsRedeemed();

    @Query(value = "SELECT COALESCE(SUM(jumlah_poin), 0) FROM poin_transaksi WHERE tipe = 'earn'", nativeQuery = true)
    int getTotalPointsEarned();

    /** Total poin EARN all-time milik satu client tertentu — untuk ditampilkan di halaman Loyalty. */
    @Query(value = "SELECT COALESCE(SUM(jumlah_poin), 0) FROM poin_transaksi WHERE client_id = :clientId AND tipe = 'earn'", nativeQuery = true)
    int getTotalPoinEarnByClient(@Param("clientId") UUID clientId);

    /** Cek apakah sudah ada record EARN untuk pesanan tertentu — guard against double-earning. */
    boolean existsByPesananPesananIdAndTipe(UUID pesananId, com.aromasenja.domain.poin.entity.TipePoin tipe);
}
