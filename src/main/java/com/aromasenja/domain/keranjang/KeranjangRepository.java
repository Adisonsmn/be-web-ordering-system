package com.aromasenja.domain.keranjang;

import com.aromasenja.domain.keranjang.entity.Keranjang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface KeranjangRepository extends JpaRepository<Keranjang, UUID> {

    @Query("SELECT k FROM Keranjang k LEFT JOIN FETCH k.detailKeranjang dk LEFT JOIN FETCH dk.menu m LEFT JOIN FETCH m.promo " +
           "WHERE k.client.clientId = :clientId")
    Optional<Keranjang> findByClientIdWithDetails(@Param("clientId") UUID clientId);

    @Query("SELECT k FROM Keranjang k LEFT JOIN FETCH k.detailKeranjang dk LEFT JOIN FETCH dk.menu m LEFT JOIN FETCH m.promo " +
           "WHERE k.sessionId = :sessionId")
    Optional<Keranjang> findBySessionIdWithDetails(@Param("sessionId") UUID sessionId);
}
