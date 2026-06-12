package com.aromasenja.config;

import com.aromasenja.common.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Konfigurasi JPA Auditing untuk field @CreatedBy / @LastModifiedBy.
 * AuditorAware membaca userId dari SecurityContext saat request berlangsung.
 *
 * Jika tidak ada authentication (endpoint public), return Optional.empty()
 * sehingga field auditing tidak diset.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                return Optional.of(userPrincipal.getUserId());
            }

            return Optional.empty();
        };
    }
}
