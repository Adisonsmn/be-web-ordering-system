package com.aromasenja.domain.pesanan.entity;

import com.aromasenja.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "detail_pesanan")
@Getter @Setter @NoArgsConstructor
public class DetailPesanan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "detail_pesanan_id")
    private UUID detailPesananId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesanan_id", nullable = false)
    private Pesanan pesanan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "catatan")
    private String catatan;

    @Column(name = "harga_snapshot", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaSnapshot;

    @Column(name = "harga_setelah_diskon", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaSetelahDiskon;

    @Column(name = "sub_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal subTotal;
}
