package com.aromasenja.domain.menu.entity;

import com.aromasenja.domain.promo.entity.Promo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menus")
@Getter @Setter @NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "description")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private Promo promo;

    @Column(name = "title_line1")
    private String titleLine1;

    @Column(name = "title_line2")
    private String titleLine2;

    @Column(name = "long_description")
    private String longDescription;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(name = "show_doneness")
    private Boolean showDoneness = false;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "doneness_options", columnDefinition = "text[]")
    private List<String> donenessOptions;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "spice_options", columnDefinition = "text[]")
    private List<String> spiceOptions;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
