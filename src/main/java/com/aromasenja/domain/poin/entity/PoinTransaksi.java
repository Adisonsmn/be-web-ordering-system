package com.aromasenja.domain.poin.entity;

import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.user.entity.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "poin_transaksi")
@Getter @Setter @NoArgsConstructor
public class PoinTransaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "poin_transaksi_id")
    private UUID poinTransaksiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesanan_id", nullable = false)
    private Pesanan pesanan;

    @Column(name = "jumlah_poin", nullable = false)
    private Integer jumlahPoin;

    @Column(name = "tipe", nullable = false)
    private TipePoin tipe;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
