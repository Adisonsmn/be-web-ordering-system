package com.aromasenja.domain.pesanan;

import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications untuk filter admin pesanan.
 * Menggantikan native SQL query yang gagal di PostgreSQL ketika parameter NULL
 * karena prepared statement tidak bisa infer tipe data parameter null.
 */
public class PesananSpecification {

    private PesananSpecification() {}

    public static Specification<Pesanan> buildFilter(
            StatusPesanan status,
            List<StatusPesanan> statuses,
            UUID mejaId,
            LocalDate tanggal,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String category) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Distinct untuk hindari duplikat akibat JOIN
            query.distinct(true);

            // Filter status: list (statuses) lebih prioritas dari single status
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            } else if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter meja
            if (mejaId != null) {
                predicates.add(cb.equal(root.get("meja").get("mejaId"), mejaId));
            }

            // Filter tanggal spesifik (prioritas di atas startDate/endDate)
            if (tanggal != null) {
                LocalDateTime startOfDay = tanggal.atStartOfDay();
                LocalDateTime endOfDay = tanggal.atTime(23, 59, 59, 999_999_999);
                predicates.add(cb.between(root.get("tanggalPesanan"), startOfDay, endOfDay));
            } else {
                // Filter rentang waktu
                if (startDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("tanggalPesanan"), startDate));
                }
                if (endDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("tanggalPesanan"), endDate));
                }
            }

            // Filter berdasarkan kategori menu (JOIN ke detailPesanan → menu)
            if (category != null && !category.isBlank()) {
                Join<Object, Object> detail = root.join("detailPesanan", JoinType.INNER);
                Join<Object, Object> menu = detail.join("menu", JoinType.INNER);
                predicates.add(cb.equal(
                    cb.upper(menu.get("category")),
                    category.toUpperCase()
                ));
            }

            // Default order: terbaru dulu
            query.orderBy(cb.desc(root.get("tanggalPesanan")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
