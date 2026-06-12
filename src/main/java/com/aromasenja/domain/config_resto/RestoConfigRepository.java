package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.entity.RestoConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestoConfigRepository extends JpaRepository<RestoConfig, UUID> {
    Optional<RestoConfig> findFirstBy();
}
