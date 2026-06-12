package com.aromasenja.domain.keranjang.entity;

import com.aromasenja.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "detail_keranjang")
@Getter @Setter @NoArgsConstructor
public class DetailKeranjang {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "detail_keranjang_id")
    private UUID detailKeranjangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keranjang_id", nullable = false)
    private Keranjang keranjang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "catatan")
    private String catatan;
}
