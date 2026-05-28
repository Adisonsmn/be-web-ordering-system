package com.aromasenja.domain.pesanan.entity;

import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.user.entity.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pesanan")
@Getter @Setter @NoArgsConstructor
public class Pesanan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pesanan_id")
    private UUID pesananId;

    @Column(name = "kode_pesanan", nullable = false, unique = true)
    private String kodePesanan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meja_id")
    private Meja meja;

    @Column(name = "tanggal_pesanan", nullable = false, updatable = false)
    private LocalDateTime tanggalPesanan;

    @Column(name = "is_served", nullable = false)
    private boolean isServed = false;

    @Column(name = "total_harga", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHarga = BigDecimal.ZERO;

    @Column(name = "jumlah_dibayar", precision = 15, scale = 2)
    private BigDecimal jumlahDibayar;

    @Column(name = "status", nullable = false)
    private StatusPesanan status = StatusPesanan.NEW;

    @Column(name = "catatan_dapur")
    private String catatanDapur;

    @Column(name = "estimasi_menit")
    private Integer estimasiMenit = 0;

    @Column(name = "metode_pembayaran")
    private MetodePembayaran metodePembayaran;

    @Column(name = "poin_digunakan", nullable = false)
    private Integer poinDigunakan = 0;

    @Column(name = "potongan_poin", nullable = false, precision = 15, scale = 2)
    private BigDecimal potonganPoin = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pesanan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailPesanan> detailPesanan = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.tanggalPesanan = LocalDateTime.now();
    }
}
