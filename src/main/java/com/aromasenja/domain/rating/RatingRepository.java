package com.aromasenja.domain.rating;

import com.aromasenja.domain.rating.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    boolean existsByPesananPesananId(UUID pesananId);

    List<Rating> findByMenuMenuIdAndIsPublicTrue(UUID menuId);

    @Query(value = "SELECT COALESCE(AVG(bintang), 0.0) FROM rating WHERE menu_id = :menuId AND is_public = true", nativeQuery = true)
    double getAverageRatingForMenu(@Param("menuId") UUID menuId);

    @Query("SELECT COALESCE(AVG(r.bintang), 0.0) FROM Rating r WHERE r.isOverall = true AND r.createdAt BETWEEN :start AND :end")
    double getAverageRatingByCreatedAtBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query(value = "SELECT bintang, COUNT(rating_id) as count FROM rating WHERE is_overall = true GROUP BY bintang", nativeQuery = true)
    List<Map<String, Object>> getRatingDistributionRaw();
}
