package com.aromasenja.domain.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_pairings")
@Getter @Setter @NoArgsConstructor
public class MenuPairing {

    @EmbeddedId
    private MenuPairingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("menuId")
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pairingMenuId")
    @JoinColumn(name = "pairing_menu_id")
    private Menu pairingMenu;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
