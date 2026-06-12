package com.aromasenja.domain.meja;

import com.aromasenja.domain.meja.entity.MejaSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MejaSessionRepository extends JpaRepository<MejaSession, UUID> {
    
    Optional<MejaSession> findByMeja_MejaIdAndIsActiveTrue(UUID mejaId);
    
    Optional<MejaSession> findByDeviceTokenAndIsActiveTrue(String deviceToken);

    @Modifying
    @Query("UPDATE MejaSession s SET s.isActive = false WHERE s.meja.mejaId = :mejaId AND s.isActive = true")
    void deactivateSessionByMejaId(@Param("mejaId") UUID mejaId);
}
