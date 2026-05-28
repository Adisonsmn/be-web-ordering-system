package com.aromasenja.domain.poin;

import com.aromasenja.domain.poin.entity.PoinTransaksi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface PoinTransaksiRepository extends JpaRepository<PoinTransaksi, UUID> {

    Page<PoinTransaksi> findByClientClientIdOrderByCreatedAtDesc(UUID clientId, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(jumlah_poin), 0) FROM poin_transaksi WHERE tipe = 'redeem'", nativeQuery = true)
    int getTotalPointsRedeemed();
}
