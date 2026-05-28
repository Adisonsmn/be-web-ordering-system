package com.aromasenja.domain.menu;

import com.aromasenja.domain.menu.entity.MenuPairing;
import com.aromasenja.domain.menu.entity.MenuPairingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface MenuPairingRepository extends JpaRepository<MenuPairing, MenuPairingId> {

    @Query("SELECT mp FROM MenuPairing mp JOIN FETCH mp.pairingMenu m LEFT JOIN FETCH m.promo " +
           "WHERE mp.id.menuId = :menuId AND m.isActive = true AND m.isAvailable = true")
    List<MenuPairing> findPairingsByMenuId(@Param("menuId") UUID menuId);
}
