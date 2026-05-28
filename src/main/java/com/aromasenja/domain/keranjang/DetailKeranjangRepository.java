package com.aromasenja.domain.keranjang;

import com.aromasenja.domain.keranjang.entity.DetailKeranjang;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DetailKeranjangRepository extends JpaRepository<DetailKeranjang, UUID> {
}
