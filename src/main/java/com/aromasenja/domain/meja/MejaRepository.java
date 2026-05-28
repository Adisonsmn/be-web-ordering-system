package com.aromasenja.domain.meja;

import com.aromasenja.domain.meja.entity.Meja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MejaRepository extends JpaRepository<Meja, UUID> {
    List<Meja> findByIsActiveTrue();
    boolean existsByNomorMeja(Integer nomorMeja);
    Optional<Meja> findByMejaIdAndIsActiveTrue(UUID mejaId);
    
    long countByIsActiveTrueAndIsOccupiedTrue();

    @Modifying
    @Query("UPDATE Meja m SET m.isActive = false WHERE m.mejaId = :id")
    void softDelete(@Param("id") UUID id);
}
