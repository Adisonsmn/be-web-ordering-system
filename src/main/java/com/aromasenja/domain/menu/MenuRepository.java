package com.aromasenja.domain.menu;

import com.aromasenja.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.promo WHERE m.isActive = true " +
           "AND (:category IS NULL OR m.category = :category) " +
           "AND (:search IS NULL OR LOWER(m.menuName) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')) " +
           "    OR LOWER(m.description) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))) " +
           "AND (:isAvailable IS NULL OR m.isAvailable = :isAvailable)")
    List<Menu> findActiveMenusWithPromo(
        @Param("category") String category,
        @Param("search") String search,
        @Param("isAvailable") Boolean isAvailable
    );

    boolean existsByMenuNameAndCategoryAndIsActiveTrue(String menuName, String category);

    Optional<Menu> findByMenuIdAndIsActiveTrue(UUID menuId);

    @Modifying
    @Query("UPDATE Menu m SET m.isActive = false WHERE m.menuId = :id")
    void softDelete(@Param("id") UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM detail_pesanan dp " +
                   "JOIN pesanan p ON dp.pesanan_id = p.pesanan_id " +
                   "WHERE dp.menu_id = :menuId AND p.status IN ('new', 'preparing', 'ready'))", 
           nativeQuery = true)
    boolean isMenuInActiveOrders(@Param("menuId") UUID menuId);

    @Query(value = "SELECT COALESCE(AVG(bintang), 0.0) FROM rating WHERE menu_id = :menuId AND is_public = true", nativeQuery = true)
    double getAverageRatingForMenu(@Param("menuId") UUID menuId);
}
