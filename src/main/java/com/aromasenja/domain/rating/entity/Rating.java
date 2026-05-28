package com.aromasenja.domain.rating.entity;

import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.user.entity.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rating")
@Getter @Setter @NoArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rating_id")
    private UUID ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesanan_id", nullable = false)
    private Pesanan pesanan;

    @Column(name = "bintang", nullable = false)
    private Short bintang;

    @Column(name = "ulasan")
    private String ulasan;

    @Column(name = "is_overall", nullable = false)
    private boolean isOverall = false;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
