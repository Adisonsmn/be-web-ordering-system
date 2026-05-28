package com.aromasenja.domain.keranjang.entity;

import com.aromasenja.domain.user.entity.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "keranjang")
@Getter @Setter @NoArgsConstructor
public class Keranjang {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "keranjang_id")
    private UUID keranjangId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", unique = true)
    private Client client;

    @Column(name = "session_id")
    private UUID sessionId;

    @OneToMany(mappedBy = "keranjang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailKeranjang> detailKeranjang = new ArrayList<>();
}
